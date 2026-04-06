package io.github.artavazdkhachatryan.markdowneditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MarkdownEditorController {

    private var sender: ((type: String, payload: String) -> Unit)? = null

    var currentContent: String by mutableStateOf("")
        private set

    internal fun attach(sender: (type: String, payload: String) -> Unit) {
        this.sender = sender
    }

    internal fun detach() {
        sender = null
    }

    internal fun onContentChanged(markdown: String) {
        currentContent = markdown
    }

    fun setContent(markdown: String) {
        sender?.invoke("setContent", markdown)
    }

    fun setReadOnly(readOnly: Boolean) {
        sender?.invoke("setReadOnly", readOnly.toString())
    }

    fun focus() {
        sender?.invoke("focus", "")
    }

    fun undo() = applyFormat("undo")
    fun redo() = applyFormat("redo")

    fun toggleBold() = applyFormat("bold")
    fun toggleItalic() = applyFormat("italic")
    fun toggleStrikethrough() = applyFormat("strikethrough")
    fun toggleCode() = applyFormat("code")
    fun toggleBlockquote() = applyFormat("blockquote")
    fun toggleBulletList() = applyFormat("bulletList")
    fun toggleOrderedList() = applyFormat("orderedList")
    fun toggleCodeBlock() = applyFormat("codeBlock")
    fun toggleHeading(level: Int) = applyFormat("h$level")

    private fun applyFormat(action: String) {
        sender?.invoke("applyFormat", action)
    }
}

@Composable
fun rememberMarkdownEditorController(): MarkdownEditorController {
    return remember { MarkdownEditorController() }
}
