package com.nuvio.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.nuvio.app.features.player.PlayerLaunch
import com.nuvio.app.features.player.PlayerLaunchStore
import com.nuvio.app.features.player.buildVidlyPlayerUrl
import com.nuvio.app.features.streams.StreamLaunchStore
import com.nuvio.app.navigation.NuvioNavigator
import com.nuvio.app.navigation.PlayerRoute
import com.nuvio.app.navigation.StreamRoute

@Composable
internal fun StreamDestination(
    route: StreamRoute,
    navController: NuvioNavigator,
    p2pEnabled: Boolean,
    openExternalPlayback: suspend (PlayerLaunch) -> Boolean,
    openExternalStreamUrl: (String) -> Boolean,
    onLandscapeLoadingChanged: (Boolean) -> Unit,
) {
    val onBack = rememberGuardedPopBackStack(navController, route)
    val launch = remember(route.launchId) { StreamLaunchStore.get(route.launchId) }
    if (launch == null) {
        LaunchedEffect(route.launchId) { onBack() }
        return
    }
    LaunchedEffect(route.launchId) {
        val vidlyUrl = buildVidlyPlayerUrl(
            videoId = launch.videoId,
            parentMetaId = launch.parentMetaId,
            contentType = launch.type,
            seasonNumber = launch.seasonNumber,
            episodeNumber = launch.episodeNumber,
            initialPositionMs = launch.resumePositionMs ?: 0L,
        )
        val playerLaunch = PlayerLaunch(
            profileId = launch.profileId,
            title = launch.title,
            sourceUrl = vidlyUrl,
            sourceHeaders = emptyMap(),
            sourceResponseHeaders = emptyMap(),
            streamType = launch.type,
            logo = launch.logo,
            poster = launch.poster,
            background = launch.background,
            seasonNumber = launch.seasonNumber,
            episodeNumber = launch.episodeNumber,
            episodeTitle = launch.episodeTitle,
            episodeThumbnail = launch.episodeThumbnail,
            streamTitle = launch.title,
            streamSubtitle = launch.episodeTitle,
            pauseDescription = launch.pauseDescription,
            providerName = "SingularityTV",
            contentType = launch.type,
            videoId = launch.videoId,
            parentMetaId = launch.parentMetaId ?: launch.videoId,
            parentMetaType = launch.parentMetaType ?: launch.type,
            initialPositionMs = launch.resumePositionMs ?: 0L,
            initialProgressFraction = launch.resumeProgressFraction,
        )
        val launchId = PlayerLaunchStore.put(playerLaunch)
        navController.navigate(PlayerRoute(launchId = launchId, title = playerLaunch.title)) {
            popUpTo<StreamRoute> { inclusive = true }
        }
    }
}
