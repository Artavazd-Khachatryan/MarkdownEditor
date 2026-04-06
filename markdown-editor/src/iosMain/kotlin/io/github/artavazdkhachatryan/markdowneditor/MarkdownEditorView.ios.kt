package io.github.artavazdkhachatryan.markdowneditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSBundle
import platform.UIKit.UIUserInterfaceStyle
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

private const val BRIDGE_NAME = "iosBridge"

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MarkdownEditorView(
    modifier: Modifier,
    controller: MarkdownEditorController?,
    initialContent: String,
    placeholder: String,
    readOnly: Boolean,
    darkMode: Boolean,
    onReady: () -> Unit,
    onContentChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onError: (String) -> Unit,
) {
    val onReadyState by rememberUpdatedState(onReady)
    val onContentChangedState by rememberUpdatedState(onContentChanged)
    val onFocusChangedState by rememberUpdatedState(onFocusChanged)
    val onErrorState by rememberUpdatedState(onError)

    var editorReady by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WKWebView?>(null) }

    // Keep handler in remember so Kotlin/Native GC does not collect it
    // while WKUserContentController holds an ObjC strong reference to it
    val handler = remember {
        MilkdownMessageHandler(
            onReady = {
                editorReady = true
                onReadyState()
            },
            onContentChanged = { markdown ->
                controller?.onContentChanged(markdown)
                onContentChangedState(markdown)
            },
            onFocusChanged = { onFocusChangedState(it) },
            onError = { onErrorState(it) },
        )
    }

    @OptIn(ExperimentalComposeUiApi::class)
    UIKitView(
        modifier = modifier,
        factory = {
            val config = WKWebViewConfiguration()
            config.userContentController.addScriptMessageHandler(handler, name = BRIDGE_NAME)

            val webView = WKWebView(frame = CGRectZero.readValue(), configuration = config)
            webView.scrollView.bounces = true
            webView.scrollView.delaysContentTouches = false

            // Resources live inside MarkdownEditor.framework, not in the main bundle
            val frameworkBundle = NSBundle.bundleWithPath(
                NSBundle.mainBundle.bundlePath + "/Frameworks/MarkdownEditor.framework"
            )
            val htmlUrl = frameworkBundle?.URLForResource(
                name = "milkdown-editor",
                withExtension = "html",
                subdirectory = "offline",
            )

            if (htmlUrl != null) {
                val baseUrl = htmlUrl.URLByDeletingLastPathComponent()
                webView.loadFileURL(htmlUrl, allowingReadAccessToURL = baseUrl ?: htmlUrl)
            }

            webViewRef = webView
            webView
        },
        update = { webView ->
            webViewRef = webView
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
        ),
    )

    LaunchedEffect(editorReady) {
        if (editorReady) {
            val webView = webViewRef ?: return@LaunchedEffect
            controller?.attach { type, payload -> webView.sendEditorMessage(type, payload) }
        }
    }

    LaunchedEffect(editorReady, initialContent) {
        if (editorReady && initialContent.isNotEmpty()) {
            webViewRef?.sendEditorMessage("setContent", initialContent)
        }
    }

    LaunchedEffect(editorReady, placeholder) {
        if (editorReady) {
            webViewRef?.sendEditorMessage("setPlaceholder", placeholder)
        }
    }

    LaunchedEffect(editorReady, readOnly) {
        if (editorReady) {
            webViewRef?.sendEditorMessage("setReadOnly", readOnly.toString())
        }
    }

    LaunchedEffect(webViewRef, darkMode) {
        webViewRef?.overrideUserInterfaceStyle =
            if (darkMode) UIUserInterfaceStyle.UIUserInterfaceStyleDark
            else UIUserInterfaceStyle.UIUserInterfaceStyleLight
    }

    DisposableEffect(Unit) {
        onDispose {
            controller?.detach()
            webViewRef?.configuration?.userContentController
                ?.removeScriptMessageHandlerForName(BRIDGE_NAME)
            webViewRef = null
        }
    }
}

internal fun WKWebView.sendEditorMessage(type: String, payload: String) {
    evaluateJavaScript(buildEditorJsCall(type, payload), completionHandler = null)
}

private class MilkdownMessageHandler(
    private val onReady: () -> Unit,
    private val onContentChanged: (String) -> Unit,
    private val onFocusChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(), WKScriptMessageHandlerProtocol {

    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage,
    ) {
        val json = didReceiveScriptMessage.body as? String ?: return
        val type = Regex("\"type\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
        val payload = Regex("\"payload\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(json)?.groupValues?.get(1)
        when (type) {
            "ready" -> onReady()
            "contentChanged" -> onContentChanged(payload?.unescape() ?: "")
            "focusChanged" -> onFocusChanged(payload == "true")
            "error" -> onError(payload?.unescape() ?: "")
        }
    }

    private fun String.unescape(): String = replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\'", "'")
        .replace("\\\\", "\\")
}
