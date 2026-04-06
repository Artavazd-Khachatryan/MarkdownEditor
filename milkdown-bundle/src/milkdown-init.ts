import { Editor, rootCtx, defaultValueCtx, editorViewOptionsCtx, editorViewCtx, parserCtx, commandsCtx } from '@milkdown/core'
import {
  commonmark,
  toggleStrongCommand,
  toggleEmphasisCommand,
  toggleInlineCodeCommand,
  wrapInBlockquoteCommand,
  wrapInBulletListCommand,
  wrapInOrderedListCommand,
  createCodeBlockCommand,
  wrapInHeadingCommand,
} from '@milkdown/preset-commonmark'
import { gfm, toggleStrikethroughCommand } from '@milkdown/preset-gfm'
import { listener, listenerCtx } from '@milkdown/plugin-listener'
import { clipboard } from '@milkdown/plugin-clipboard'
import { history, undoCommand, redoCommand } from '@milkdown/plugin-history'
import { math } from '@milkdown/plugin-math'
import { nord } from '@milkdown/theme-nord'
import '@milkdown/theme-nord/style.css'
import 'katex/dist/katex.min.css'

interface BridgeMessage {
  type: string
  payload?: string
}

interface AndroidBridge {
  onMessageReceived(json: string): void
}

declare global {
  interface Window {
    AndroidBridge?: AndroidBridge
    webkit?: {
      messageHandlers?: {
        iosBridge?: { postMessage(msg: string): void }
      }
    }
    MilkdownEditor: typeof MilkdownEditorAPI
  }
}

let editorInstance: Editor | null = null

function notifyNative(msg: BridgeMessage) {
  const json = JSON.stringify(msg)
  if (window.AndroidBridge) {
    window.AndroidBridge.onMessageReceived(json)
  } else if (window.webkit?.messageHandlers?.iosBridge) {
    window.webkit.messageHandlers.iosBridge.postMessage(json)
  }
}

const MilkdownEditorAPI = {
  async init(initialContent: string, readOnly: boolean, darkMode: boolean) {
    try {
      editorInstance = await Editor.make()
        .config(ctx => {
          ctx.set(rootCtx, '#editor')
          ctx.set(defaultValueCtx, initialContent || '')
          ctx.update(editorViewOptionsCtx, prev => ({
            ...prev,
            editable: () => !readOnly,
            handleDOMEvents: {
              ...prev.handleDOMEvents,
              focus: () => { notifyNative({ type: 'focusChanged', payload: 'true' }); return false },
              blur: () => { notifyNative({ type: 'focusChanged', payload: 'false' }); return false },
            },
          }))
        })
        .config(nord)
        .use(commonmark)
        .use(gfm)
        .use(math)
        .use(clipboard)
        .use(history)
        .use(listener)
        .config(ctx => {
          ctx.get(listenerCtx).markdownUpdated((_, markdown) => {
            const isEmpty = !markdown || markdown.trim() === ''
            const prosemirror = document.querySelector('.ProseMirror')
            if (prosemirror) {
              prosemirror.classList.toggle('is-empty', isEmpty)
            }
            notifyNative({ type: 'contentChanged', payload: markdown })
          })
        })
        .create()

      // Set initial empty state for placeholder
      const prosemirror = document.querySelector('.ProseMirror')
      if (prosemirror) {
        const isEmpty = !initialContent || initialContent.trim() === ''
        prosemirror.classList.toggle('is-empty', isEmpty)
      }
      notifyNative({ type: 'ready' })
    } catch (e) {
      notifyNative({ type: 'error', payload: String(e) })
    }
  },

  setContent(markdown: string) {
    if (!editorInstance) return
    editorInstance.action(ctx => {
      const view = ctx.get(editorViewCtx)
      const parser = ctx.get(parserCtx)
      const doc = parser(markdown)
      if (doc) {
        const { state } = view
        const tr = state.tr
          .replaceWith(0, state.doc.content.size, doc.content)
          .scrollIntoView()
        view.dispatch(tr)
      }
    })
  },

  focus() {
    if (!editorInstance) return
    editorInstance.action(ctx => {
      ctx.get(editorViewCtx).focus()
    })
  },

  applyFormat(action: string) {
    if (!editorInstance) return
    editorInstance.action(ctx => {
      const commands = ctx.get(commandsCtx)
      switch (action) {
        case 'bold':          commands.call(toggleStrongCommand.key); break
        case 'italic':        commands.call(toggleEmphasisCommand.key); break
        case 'strikethrough': commands.call(toggleStrikethroughCommand.key); break
        case 'code':          commands.call(toggleInlineCodeCommand.key); break
        case 'blockquote':    commands.call(wrapInBlockquoteCommand.key); break
        case 'bulletList':    commands.call(wrapInBulletListCommand.key); break
        case 'orderedList':   commands.call(wrapInOrderedListCommand.key); break
        case 'codeBlock': {
          // createCodeBlockCommand inserts a new block with surrounding empty paragraphs.
          // Instead, convert the current paragraph to a code block in place.
          const view = ctx.get(editorViewCtx)
          const { state, dispatch } = view
          const codeBlockType = state.schema.nodes['code_block']
          if (codeBlockType) {
            const tr = state.tr.setBlockType(
              state.selection.from,
              state.selection.to,
              codeBlockType,
            )
            dispatch(tr)
          } else {
            commands.call(createCodeBlockCommand.key)
          }
          break
        }
        case 'h1':            commands.call(wrapInHeadingCommand.key, 1); break
        case 'h2':            commands.call(wrapInHeadingCommand.key, 2); break
        case 'h3':            commands.call(wrapInHeadingCommand.key, 3); break
        case 'undo':          commands.call(undoCommand.key); break
        case 'redo':          commands.call(redoCommand.key); break
      }
    })
  },

  setReadOnly(enabled: boolean) {
    if (!editorInstance) return
    editorInstance.action(ctx => {
      const view = ctx.get(editorViewCtx)
      view.setProps({ editable: () => !enabled })
    })
  },

  setPlaceholder(text: string) {
    const prosemirror = document.querySelector('.ProseMirror')
    if (prosemirror) {
      prosemirror.setAttribute('data-placeholder', text)
    }
  },

  handleMessage(json: string) {
    try {
      const msg: BridgeMessage = JSON.parse(json)
      switch (msg.type) {
        case 'setContent':
          MilkdownEditorAPI.setContent(msg.payload ?? '')
          break
        case 'focus':
          MilkdownEditorAPI.focus()
          break
        case 'applyFormat':
          MilkdownEditorAPI.applyFormat(msg.payload ?? '')
          break
        case 'setReadOnly':
          MilkdownEditorAPI.setReadOnly(msg.payload === 'true')
          break
        case 'setPlaceholder':
          MilkdownEditorAPI.setPlaceholder(msg.payload ?? '')
          break
      }
    } catch (e) {
      notifyNative({ type: 'error', payload: String(e) })
    }
  },
}

window.MilkdownEditor = MilkdownEditorAPI
