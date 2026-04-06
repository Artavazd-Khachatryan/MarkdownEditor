package io.github.artavazdkhachatryan.markdowneditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController

private val SAMPLE_CONTENT = """
# Markdown Editor
A **rich text** editor powered by [Milkdown](https://milkdown.dev).

## Features
- **Bold**, *italic*, ~~strikethrough~~
- `inline code` and code blocks
- Tables, task lists, and more

## Math
Inline math: ${'$'}E = mc^2${'$'}

Block math:
${'$'}${'$'}
\int_0^\infty e^{-x^2} dx = \frac{\sqrt{\pi}}{2}
${'$'}${'$'}
""".trimIndent()

fun MarkdownEditorDemoViewController() = ComposeUIViewController {
    val controller = rememberMarkdownEditorController()
    var readOnly by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Markdown Editor",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (focused) "Focused" else "Unfocused",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (focused) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                    )
                    TextButton(onClick = { darkMode = !darkMode }) {
                        Text(if (darkMode) "Light" else "Dark")
                    }
                    TextButton(onClick = { readOnly = !readOnly }) {
                        Text(if (readOnly) "Edit" else "Lock")
                    }
                }
            }

            HorizontalDivider()

            // Editor
            MarkdownEditorView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                controller = controller,
                placeholder = "Start writing markdown...",
                readOnly = readOnly,
                darkMode = darkMode,
                onFocusChanged = { focused = it },
            )

            HorizontalDivider()

            // Formatting toolbar — visible only when focused and editable
            AnimatedVisibility(visible = focused && !readOnly) {
                FormattingToolbar(controller = controller)
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { controller.setContent(SAMPLE_CONTENT) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Load sample")
                }
                Button(
                    onClick = { controller.focus() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Focus editor")
                }
            }
        }
    }
}

@Composable
private fun FormattingToolbar(controller: MarkdownEditorController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FormatChip("↩ Undo", onClick = { controller.undo() })
        FormatChip("↪ Redo", onClick = { controller.redo() })
        FormatChip("B", onClick = { controller.toggleBold() })
        FormatChip("I", onClick = { controller.toggleItalic() })
        FormatChip("S", onClick = { controller.toggleStrikethrough() })
        FormatChip("<>", onClick = { controller.toggleCode() })
        FormatChip("H1", onClick = { controller.toggleHeading(1) })
        FormatChip("H2", onClick = { controller.toggleHeading(2) })
        FormatChip("H3", onClick = { controller.toggleHeading(3) })
        FormatChip("• List", onClick = { controller.toggleBulletList() })
        FormatChip("1. List", onClick = { controller.toggleOrderedList() })
        FormatChip("Quote", onClick = { controller.toggleBlockquote() })
        FormatChip("Code block", onClick = { controller.toggleCodeBlock() })
    }
}

@Composable
private fun FormatChip(label: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
    )
}
