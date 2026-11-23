package com.nuvio.app.features.player

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformVidlyWebPlayer(
    url: String,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    UIKitView(
        modifier = modifier.background(Color.Black),
        factory = {
            val controller = WKUserContentController()
            controller.addUserScript(
                WKUserScript(
                    source = VIDLY_INJECTION_JS,
                    injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentEnd,
                    forMainFrameOnly = false,
                )
            )
            val config = WKWebViewConfiguration().apply {
                userContentController = controller
                allowsInlineMediaPlayback = true
                mediaTypesRequiringUserActionForPlayback = 0uL
            }
            WKWebView(frame = CGRectZero, configuration = config).apply {
                NSURL.URLWithString(url)?.let { loadRequest(NSURLRequest.requestWithURL(it)) }
            }
        }
    )
}
