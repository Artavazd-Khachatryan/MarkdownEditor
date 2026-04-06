package io.github.artavazdkhachatryan.markdowneditor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController

/**
 * Creates a [UIViewController] that hosts the Markdown editor.
 *
 * ### Swift usage
 *
 * **Basic:**
 * ```swift
 * let config = MarkdownEditorConfig()
 * config.initialContent = "# Hello"
 * config.onContentChanged = { markdown in self.save(markdown as String) }
 *
 * let vc = MarkdownEditorViewControllerKt.MarkdownEditorViewController(config: config)
 * ```
 *
 * **With controller for imperative control:**
 * ```swift
 * let controller = MarkdownEditorController()
 * let config = MarkdownEditorConfig()
 * config.placeholder = "Start writing..."
 * config.onFocusChanged = { focused in self.setToolbarVisible(focused as! Bool) }
 *
 * let vc = MarkdownEditorViewControllerKt.MarkdownEditorViewController(
 *     controller: controller,
 *     config: config
 * )
 *
 * // Later:
 * controller.setContent("## Updated")
 * controller.toggleBold()
 * controller.focus()
 * ```
 *
 * @param controller Optional [MarkdownEditorController] for imperative control.
 *   Create one via `MarkdownEditorController()` and retain it to call methods after creation.
 * @param config Configuration including initial content, callbacks, and display options.
 *   All fields have defaults — only set what you need.
 */
fun MarkdownEditorViewController(
    controller: MarkdownEditorController = MarkdownEditorController(),
    config: MarkdownEditorConfig = MarkdownEditorConfig(),
) = ComposeUIViewController {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
        ) {
            MarkdownEditorView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                controller = controller,
                initialContent = config.initialContent,
                placeholder = config.placeholder,
                readOnly = config.readOnly,
                darkMode = config.darkMode,
                onReady = config.onReady,
                onContentChanged = config.onContentChanged,
                onFocusChanged = config.onFocusChanged,
                onError = config.onError,
            )
        }
    }
}
