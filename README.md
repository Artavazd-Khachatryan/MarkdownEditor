# MarkdownEditor

A Kotlin Multiplatform (KMP) Markdown editor component for Android and iOS, built with Jetpack Compose Multiplatform and powered by [Milkdown](https://milkdown.dev/).

## Features

- **WYSIWYG Markdown editing** — rich text editing backed by a Milkdown-powered web view
- **Cross-platform** — single API for Android and iOS via Compose Multiplatform
- **Formatting toolbar** — bold, italic, strikethrough, code, headings (H1–H3), block quote, bullet list, ordered list, code block
- **Math support** — inline and block LaTeX via KaTeX
- **Dark mode** — adapts to system theme
- **Read-only mode** — display Markdown without editing
- **Placeholder text** — customizable hint shown when the editor is empty
- **Undo / Redo** — full history support
- **Focus management** — programmatic focus control and focus-state callbacks

## Installation

### Android (Maven / JitPack)

Add the GitHub Packages repository to your project and declare the dependency:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://artavazdkhachatryan.github.io/MarkdownEditor/")
    }
}
```

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.github.artavazdkhachatryan:markdown-editor-android:<latest-version>")
}
```

### iOS (Swift Package Manager)

Add the package to your Xcode project via **File → Add Package Dependencies** using the repository URL, or add it to your `Package.swift`:

```swift
dependencies: [
    .package(url: "https://github.com/ArtavazdKhachatryan/MarkdownEditor", from: "<latest-version>")
]
```

### Kotlin Multiplatform

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.artavazdkhachatryan:markdown-editor:<latest-version>")
        }
    }
}
```

## Usage

### Android (Compose)

#### Minimal setup

```kotlin
@Composable
fun MyScreen() {
    val controller = rememberMarkdownEditorController()

    MarkdownEditorView(
        modifier = Modifier.fillMaxSize(),
        controller = controller,
        placeholder = "Start writing markdown...",
    )
}
```

#### All parameters

```kotlin
MarkdownEditorView(
    modifier = Modifier.fillMaxSize(),
    controller = controller,
    initialContent = "# Hello\nThis is **Markdown**.",
    placeholder = "Start writing...",
    readOnly = false,
    darkMode = isSystemInDarkTheme(),
    onReady = { /* editor is initialized */ },
    onContentChanged = { markdown -> /* called on every edit */ },
    onFocusChanged = { focused -> /* true when editor has focus */ },
    onError = { message -> /* handle errors */ },
)
```

#### Reading and setting content

```kotlin
// Set content programmatically
controller.setContent("# Hello\nThis is **Markdown**.")

// Read the current content (Compose state — observe it directly)
val markdown: String = controller.currentContent
```

#### Formatting toolbar example

```kotlin
@Composable
fun FormattingToolbar(controller: MarkdownEditorController) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Button(onClick = { controller.undo() })          { Text("Undo") }
        Button(onClick = { controller.redo() })          { Text("Redo") }
        Button(onClick = { controller.toggleBold() })    { Text("B") }
        Button(onClick = { controller.toggleItalic() })  { Text("I") }
        Button(onClick = { controller.toggleStrikethrough() }) { Text("S") }
        Button(onClick = { controller.toggleCode() })    { Text("<>") }
        Button(onClick = { controller.toggleHeading(1) }) { Text("H1") }
        Button(onClick = { controller.toggleHeading(2) }) { Text("H2") }
        Button(onClick = { controller.toggleHeading(3) }) { Text("H3") }
        Button(onClick = { controller.toggleBulletList() })  { Text("• List") }
        Button(onClick = { controller.toggleOrderedList() }) { Text("1. List") }
        Button(onClick = { controller.toggleBlockquote() })  { Text("Quote") }
        Button(onClick = { controller.toggleCodeBlock() })   { Text("Code block") }
    }
}
```

#### Full Android Activity example

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val controller = rememberMarkdownEditorController()
            var readOnly by remember { mutableStateOf(false) }
            var darkMode by remember { mutableStateOf(false) }
            var focused by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxSize()) {
                MarkdownEditorView(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    controller = controller,
                    placeholder = "Start writing markdown...",
                    readOnly = readOnly,
                    darkMode = darkMode,
                    onFocusChanged = { focused = it },
                )

                // Show toolbar only when editor is focused and editable
                AnimatedVisibility(visible = focused && !readOnly) {
                    FormattingToolbar(controller = controller)
                }

                Row(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = { readOnly = !readOnly }) {
                        Text(if (readOnly) "Edit" else "Read-only")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { darkMode = !darkMode }) {
                        Text(if (darkMode) "Light" else "Dark")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { controller.focus() }) {
                        Text("Focus")
                    }
                }
            }
        }
    }
}
```

### iOS (UIKit)

```swift
import UIKit
import MarkdownEditor

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        window = UIWindow(windowScene: windowScene)

        let config = MarkdownEditorConfig()
        config.placeholder = "Tap here to start writing..."
        config.onContentChanged = { markdown in
            print("Content changed: \(markdown ?? "")")
        }

        window?.rootViewController = MarkdownEditorViewControllerKt.MarkdownEditorViewController(
            controller: MarkdownEditorController(),
            config: config
        )
        window?.makeKeyAndVisible()
    }
}
```

### Controller API reference

| Method | Description |
|--------|-------------|
| `setContent(markdown)` | Replace the editor's content |
| `currentContent` | Current Markdown content (Compose state) |
| `focus()` | Programmatically focus the editor |
| `setReadOnly(Boolean)` | Toggle read-only mode |
| `undo()` / `redo()` | History navigation |
| `toggleBold()` | Toggle **bold** on selection |
| `toggleItalic()` | Toggle *italic* on selection |
| `toggleStrikethrough()` | Toggle ~~strikethrough~~ on selection |
| `toggleCode()` | Toggle `inline code` on selection |
| `toggleHeading(level)` | Toggle heading H1–H3 |
| `toggleBulletList()` | Toggle bullet list |
| `toggleOrderedList()` | Toggle ordered list |
| `toggleBlockquote()` | Toggle block quote |
| `toggleCodeBlock()` | Toggle fenced code block |

## Requirements

| Platform | Minimum version |
|----------|----------------|
| Android  | API 28 (Android 9) |
| iOS      | iOS 16 |

Build tools:
- JDK 17
- Kotlin 2.2.10
- Xcode (for iOS builds)
- Node.js 14+ (only if rebuilding the Milkdown bundle)

## Project Structure

```
MarkdownEditor/
├── app/                   # Android demo application
├── iosApp/                # iOS demo application (Xcode project)
├── markdown-editor/       # KMP library module
│   └── src/
│       ├── commonMain/    # Shared API (MarkdownEditorView, MarkdownEditorController)
│       ├── androidMain/   # Android WebView implementation
│       └── iosMain/       # iOS WKWebView implementation
└── milkdown-bundle/       # TypeScript source for the bundled Milkdown editor
```

## Building

### Android demo

```bash
./gradlew app:installDebug
```

### iOS XCFramework

```bash
./gradlew :markdown-editor:assembleMarkdownEditorXCFramework
```

Then open `iosApp/iosApp.xcodeproj` in Xcode and run.

### Rebuild Milkdown bundle (optional)

Only needed if you modify the web editor source:

```bash
cd milkdown-bundle
npm install
npm run build
```

The output files are copied into `markdown-editor/src/commonMain/composeResources/files/offline/`.

## How It Works

The editor is a thin Kotlin/Compose wrapper around a WebView that hosts the [Milkdown](https://milkdown.dev/) editor bundled as a self-contained HTML page. Kotlin and the web layer communicate over a native bridge:

- **Android** — `JavascriptInterface` ("AndroidBridge") + `evaluateJavascript`
- **iOS** — `WKScriptMessageHandler` ("iosBridge") + `evaluateJavaScript`

All bridge messages are JSON-encoded. The `MarkdownEditorController` exposes a clean Kotlin API and hides all bridge details.

## License

```
Copyright 2024 Artavazd Khachatryan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
