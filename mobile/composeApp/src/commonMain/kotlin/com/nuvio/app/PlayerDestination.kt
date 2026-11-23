package com.nuvio.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nuvio.app.core.ui.PlatformBackHandler
import com.nuvio.app.features.party.PartyMedia
import com.nuvio.app.features.player.ExternalPlayerIntentResult
import com.nuvio.app.features.player.PlatformVidlyWebPlayer
import com.nuvio.app.features.player.PlayerLaunch
import com.nuvio.app.features.player.PlayerLaunchStore
import com.nuvio.app.features.player.buildVidlyPlayerUrl
import com.nuvio.app.features.watchprogress.ResumePromptRepository
import com.nuvio.app.navigation.NuvioNavigator
import com.nuvio.app.navigation.PlayerRoute

@Composable
internal fun PlayerDestination(
    route: PlayerRoute,
    navController: NuvioNavigator,
    externalPlayerId: String?,
    externalPlayerNotConfiguredText: String,
    externalPlayerFailedText: String,
    onExternalPlayerLaunch: (PlayerLaunch) -> Unit,
    launchExternalPlayer: (ExternalPlayerIntentResult.Success) -> Boolean,
    openExternalStreamUrl: (String) -> Boolean,
    onOpenPartyMedia: (PartyMedia) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    PlatformBackHandler(onBack = onBack)
    val launch = remember(route.launchId) { PlayerLaunchStore.get(route.launchId) }
    if (launch == null) {
        LaunchedEffect(route.launchId) { onBack() }
        Box(modifier = Modifier.fillMaxSize())
        return
    }
    LaunchedEffect(launch.videoId) {
        launch.videoId?.let { ResumePromptRepository.markPlayerEntered(it) }
    }
    val vidlyUrl = remember(launch) {
        if (launch.sourceUrl.startsWith("https://vidly.pro/")) launch.sourceUrl
        else buildVidlyPlayerUrl(
            videoId = launch.videoId ?: launch.parentMetaId,
            parentMetaId = launch.parentMetaId,
            contentType = launch.contentType,
            seasonNumber = launch.seasonNumber,
            episodeNumber = launch.episodeNumber,
            initialPositionMs = launch.initialPositionMs,
        )
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        PlatformVidlyWebPlayer(
            url = vidlyUrl,
            onBack = onBack,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 10.dp)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
