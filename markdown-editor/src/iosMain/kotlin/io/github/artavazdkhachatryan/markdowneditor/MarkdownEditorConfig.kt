package io.github.artavazdkhachatryan.markdowneditor

/**
 * Configuration for [MarkdownEditorViewController].
 *
 * All properties have sensible defaults so Swift callers only need to set
 * what they care about:
 *
 * ```swift
 * let config = MarkdownEditorConfig()
 * config.initialContent = "# Hello"
 * config.placeholder = "Start writing..."
 * config.onContentChanged = { markdown in self.save(markdown as String) }
 * ```
 */
class MarkdownEditorConfig {
    var initialContent: String = ""
    var placeholder: String = ""
    var readOnly: Boolean = false
    var darkMode: Boolean = false
    var onReady: () -> Unit = {}
    var onContentChanged: (String) -> Unit = {}
    var onFocusChanged: (Boolean) -> Unit = {}
    var onError: (String) -> Unit = {}
}
