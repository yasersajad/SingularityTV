package com.nuvio.app.features.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal fun buildVidlyPlayerUrl(
    videoId: String,
    parentMetaId: String?,
    contentType: String?,
    seasonNumber: Int?,
    episodeNumber: Int?,
    initialPositionMs: Long,
): String {
    val rawId = (parentMetaId ?: videoId).removePrefix("tmdb:").substringBefore(":")
    val isSeries = contentType.equals("series", ignoreCase = true) ||
        contentType.equals("tv", ignoreCase = true) ||
        seasonNumber != null
    val startSec = (initialPositionMs / 1000L).toInt()
    val baseUrl = if (isSeries) {
        "https://vidly.pro/tv/$rawId/${seasonNumber ?: 1}/${episodeNumber ?: 1}"
    } else {
        "https://vidly.pro/movie/$rawId"
    }
    val params = buildList {
        add("color=e50914")
        add("autoplay=1")
        add("nextEpisode=1")
        add("audioPicker=1")
        if (startSec > 5) add("t=$startSec")
    }.joinToString("&")
    return "$baseUrl?$params"
}

internal const val VIDLY_INJECTION_JS = """
(function() {
    if (window.__sgInjected) return;
    window.__sgInjected = true;
    var s = document.createElement('style');
    s.textContent = '*{-webkit-touch-callout:none;-webkit-user-select:none;user-select:none}[class*="watermark"],[class*="brand"],button[aria-label*="Fullscreen" i]{display:none!important}';
    document.head.appendChild(s);
    var b = document.createElement('div');
    b.id = 'singularity-s-badge';
    b.innerHTML = '<svg width="26" height="26" viewBox="0 0 64 64" fill="none"><path d="M44 16H24C19.58 16 16 19.58 16 24C16 28.42 19.58 32 24 32H40C44.42 32 48 35.58 48 40C48 44.42 44.42 48 40 48H20" stroke="#FFFFFF" stroke-width="7.5" stroke-linecap="round"/></svg>';
    b.style.cssText = 'position:fixed;top:14px;right:16px;z-index:2147483647;pointer-events:none;opacity:0.92';
    document.body.appendChild(b);
    var origPlay = HTMLMediaElement.prototype.play;
    window.__sbUserPaused = false;
    document.addEventListener('pointerdown', function(e) {
        var v = document.querySelector('video');
        if (v && (e.target.closest('button') || e.target === v)) {
            setTimeout(function() { window.__sbUserPaused = v.paused; }, 60);
        }
    }, true);
    HTMLMediaElement.prototype.play = function() {
        if (window.__sbUserPaused) return Promise.reject(new DOMException('Paused', 'NotAllowedError'));
        return origPlay.apply(this, arguments);
    };
    if ('mediaSession' in navigator) {
        setInterval(function() {
            if (navigator.mediaSession.metadata) {
                var t = (navigator.mediaSession.metadata.title || '').replace(/\s*[-•|]\s*Vidly.*/i, '');
                navigator.mediaSession.metadata = new MediaMetadata({ title: t || 'SingularityTV', artist: 'SingularityTV' });
            }
        }, 1500);
    }
})();
"""

@Composable
expect fun PlatformVidlyWebPlayer(
    url: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
)
