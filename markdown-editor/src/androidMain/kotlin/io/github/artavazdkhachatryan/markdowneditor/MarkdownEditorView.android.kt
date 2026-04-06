package io.github.artavazdkhachatryan.markdowneditor

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewAssetLoader

private const val ASSET_HOST = "https://appassets.androidplatform.net"
private const val EDITOR_URL = "$ASSET_HOST/assets/offline/milkdown-editor.html"

@SuppressLint("SetJavaScriptEnabled")
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

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var editorReady by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val assetLoader = WebViewAssetLoader.Builder()
                .setDomain("appassets.androidplatform.net")
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .build()

            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                isVerticalScrollBarEnabled = true
                overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest,
                    ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
                }

                addJavascriptInterface(
                    object : Any() {
                        @JavascriptInterface
                        fun onMessageReceived(json: String) {
                            post {
                                handleInboundMessage(
                                    json = json,
                                    onReady = {
                                        editorReady = true
                                        onReadyState()
                                    },
                                    onContentChanged = { markdown ->
                                        controller?.onContentChanged(markdown)
                                        onContentChangedState(markdown)
                                    },
                                    onFocusChanged = onFocusChangedState,
                                    onError = onErrorState,
                                )
                            }
                        }
                    },
                    "AndroidBridge",
                )

                loadUrl(EDITOR_URL)
                webViewRef = this
            }
        },
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
        val webView = webViewRef ?: return@LaunchedEffect
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, darkMode)
        } else {
            WebSettingsCompat.setForceDark(
                webView.settings,
                if (darkMode) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF,
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller?.detach()
            webViewRef?.destroy()
        }
    }
}

internal fun WebView.sendEditorMessage(type: String, payload: String) {
    val jsCall = buildEditorJsCall(type, payload)
    evaluateJavascript(jsCall, null)
}

private fun handleInboundMessage(
    json: String,
    onReady: () -> Unit,
    onContentChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onError: (String) -> Unit,
) {
    try {
        val type = Regex("\"type\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
        val payload = Regex("\"payload\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(json)?.groupValues?.get(1)
        when (type) {
            "ready" -> onReady()
            "contentChanged" -> onContentChanged(payload?.unescape() ?: "")
            "focusChanged" -> onFocusChanged(payload == "true")
            "error" -> onError(payload?.unescape() ?: "")
        }
    } catch (_: Exception) {
    }
}

private fun String.unescape(): String = replace("\\n", "\n")
    .replace("\\r", "\r")
    .replace("\\'", "'")
    .replace("\\\\", "\\")
