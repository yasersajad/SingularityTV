package com.nuvio.tv.ui.screens.player

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VidlyTvWebPlayer(
    tmdbId: String,
    mediaType: String,
    season: Int? = null,
    episode: Int? = null,
    startSeconds: Int = 0,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }
    val cleanId = tmdbId.removePrefix("tmdb:").substringBefore(":")
    val isSeries = mediaType.equals("series", ignoreCase = true) ||
        mediaType.equals("tv", ignoreCase = true) ||
        season != null
    val baseUrl = if (isSeries) {
        "https://vidly.pro/tv/$cleanId/${season ?: 1}/${episode ?: 1}"
    } else {
        "https://vidly.pro/movie/$cleanId"
    }
    val query = buildList {
        add("color=e50914")
        add("autoplay=1")
        add("nextEpisode=1")
        add("audioPicker=1")
        if (startSeconds > 5) add("t=$startSeconds")
    }.joinToString("&")

    val tvScript = """
        (function() {
            if (window.__sgTvInit) return;
            window.__sgTvInit = true;
            var s = document.createElement('style');
            s.textContent = '[class*="watermark"],[class*="brand"]{display:none!important}';
            document.head.appendChild(s);
            var b = document.createElement('div');
            b.innerHTML = '<svg width="28" height="28" viewBox="0 0 64 64" fill="none"><path d="M44 16H24C19.58 16 16 19.58 16 24C16 28.42 19.58 32 24 32H40C44.42 32 48 35.58 48 40C48 44.42 44.42 48 40 48H20" stroke="#FFFFFF" stroke-width="7.5" stroke-linecap="round"/></svg>';
            b.style.cssText = 'position:fixed;top:18px;right:22px;z-index:2147483647;pointer-events:none;opacity:0.92';
            document.body.appendChild(b);
            document.addEventListener('keydown', function(e) {
                var v = document.querySelector('video');
                if (!v) return;
                if (e.key === 'MediaPlayPause' || e.key === 'Enter') {
                    if (document.activeElement && document.activeElement.tagName === 'BUTTON') return;
                    v.paused ? v.play() : v.pause();
                } else if (e.key === 'ArrowRight') {
                    v.currentTime = Math.min(v.duration || 99999, v.currentTime + 10);
                } else if (e.key === 'ArrowLeft') {
                    v.currentTime = Math.max(0, v.currentTime - 10);
                }
            }, true);
        })();
    """.trimIndent()

    AndroidView(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        factory = { context ->
            WebView(context).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val h = request?.url?.host.orEmpty()
                        return !(h.contains("vidly.pro") || h.contains("vidlink") || h.contains("vidsrc"))
                    }
                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        val u = request?.url?.toString()?.lowercase().orEmpty()
                        if (u.contains("doubleclick") || u.contains("googlesyndication") || u.contains("popads")) {
                            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))
                        }
                        return super.shouldInterceptRequest(view, request)
                    }
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(tvScript, null)
                    }
                    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                        view?.postDelayed({ view.reload() }, 1500)
                    }
                }
                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        onBack()
                        true
                    } else false
                }
                loadUrl("$baseUrl?$query")
            }
        }
    )
}
