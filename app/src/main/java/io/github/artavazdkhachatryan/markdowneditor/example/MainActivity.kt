package io.github.artavazdkhachatryan.markdowneditor.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.artavazdkhachatryan.markdowneditor.MarkdownEditorController
import io.github.artavazdkhachatryan.markdowneditor.MarkdownEditorView
import io.github.artavazdkhachatryan.markdowneditor.rememberMarkdownEditorController
import io.github.artavazdkhachatryan.markdowneditor.example.ui.theme.MarkdownEditorTheme

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

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MarkdownEditorTheme {
                val controller = rememberMarkdownEditorController()
                var readOnly by remember { mutableStateOf(false) }
                var darkMode by remember { mutableStateOf(false) }
                var focused by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Markdown Editor") },
                            actions = {
                                Text(
                                    text = if (focused) "Focused" else "Unfocused",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (focused) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(end = 4.dp),
                                )
                                IconButton(onClick = { darkMode = !darkMode }) {
                                    Icon(
                                        painter = painterResource(
                                            if (darkMode) android.R.drawable.ic_menu_day
                                            else android.R.drawable.ic_menu_month
                                        ),
                                        contentDescription = if (darkMode) "Light mode" else "Dark mode",
                                    )
                                }
                                IconButton(onClick = { readOnly = !readOnly }) {
                                    Icon(
                                        imageVector = if (readOnly) Icons.Default.Lock else Icons.Default.Edit,
                                        contentDescription = if (readOnly) "Read-only" else "Editable",
                                    )
                                }
                            },
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
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

                        AnimatedVisibility(visible = focused && !readOnly) {
                            FormattingToolbar(controller = controller)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            androidx.compose.material3.Button(
                                onClick = { controller.setContent(SAMPLE_CONTENT) },
                                modifier = Modifier.weight(1f),
                            ) { Text("Load sample") }
                            androidx.compose.material3.Button(
                                onClick = { controller.focus() },
                                modifier = Modifier.weight(1f),
                            ) { Text("Focus editor") }
                        }
                    }
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
