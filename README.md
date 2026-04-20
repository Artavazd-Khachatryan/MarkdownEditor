# MarkdownEditor

A Kotlin Multiplatform Markdown editor for Android and iOS, powered by [Milkdown](https://milkdown.dev/).

## Features

- WYSIWYG Markdown editing via a Milkdown-powered web view
- Single API for Android and iOS via Compose Multiplatform
- Formatting: bold, italic, strikethrough, code, headings, lists, block quote, code block
- Inline and block LaTeX math (KaTeX)
- Dark mode, read-only mode, placeholder text
- Undo / redo, focus management

## Installation

### Kotlin Multiplatform

```kotlin
// settings.gradle.kts
repositories {
    maven("https://artavazd-khachatryan.github.io/MarkdownEditor/")
}

// commonMain dependencies
implementation("io.github.artavazd-khachatryan:markdown-editor:<latest-version>")
```

### Android only

```kotlin
implementation("io.github.artavazd-khachatryan:markdown-editor-android:<latest-version>")
```

### iOS (Swift Package Manager)

```swift
.package(url: "https://github.com/Artavazd-Khachatryan/MarkdownEditor", from: "<latest-version>")
```

## Usage

```kotlin
@Composable
fun MyScreen() {
    val controller = rememberMarkdownEditorController()

    MarkdownEditorView(
        modifier = Modifier.fillMaxSize(),
        controller = controller,
        initialContent = "# Hello\nThis is **Markdown**.",
        placeholder = "Start writing...",
        readOnly = false,
        darkMode = isSystemInDarkTheme(),
        onContentChanged = { markdown -> /* called on every edit */ },
    )
}
```

### Controller

```kotlin
controller.setContent("# Updated")
controller.currentContent          // current Markdown (Compose state)
controller.focus()
controller.toggleBold()
controller.toggleItalic()
controller.toggleStrikethrough()
controller.toggleCode()
controller.toggleHeading(1)        // H1–H3
controller.toggleBulletList()
controller.toggleOrderedList()
controller.toggleBlockquote()
controller.toggleCodeBlock()
controller.undo()
controller.redo()
```

### iOS (UIKit)

```swift
import MarkdownEditor

let config = MarkdownEditorConfig()
config.placeholder = "Tap here to start writing..."
config.onContentChanged = { markdown in print(markdown ?? "") }

let vc = MarkdownEditorViewControllerKt.MarkdownEditorViewController(
    controller: MarkdownEditorController(),
    config: config
)
```

## Requirements

| Platform | Minimum version |
|----------|----------------|
| Android  | API 28 |
| iOS      | 16 |

## License

Apache License 2.0 — see [LICENSE](LICENSE).
