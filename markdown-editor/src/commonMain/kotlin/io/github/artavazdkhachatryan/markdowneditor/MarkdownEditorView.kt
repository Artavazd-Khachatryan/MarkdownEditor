package io.github.artavazdkhachatryan.markdowneditor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MarkdownEditorView(
    modifier: Modifier = Modifier,
    controller: MarkdownEditorController? = null,
    initialContent: String = "",
    placeholder: String = "",
    readOnly: Boolean = false,
    darkMode: Boolean = false,
    onReady: () -> Unit = {},
    onContentChanged: (String) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    onError: (String) -> Unit = {},
)
