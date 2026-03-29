package com.bruno.itunessearch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bruno.itunessearch.di.LocalAppContainer
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.album.AlbumScreen
import com.bruno.itunessearch.ui.components.MiniPlayer
import com.bruno.itunessearch.ui.components.SongBottomSheet
import com.bruno.itunessearch.ui.home.HomeScreen
import com.bruno.itunessearch.ui.player.PlayerScreen
import com.bruno.itunessearch.ui.splash.SplashScreen
import kotlinx.serialization.Serializable

// Type-safe navigation routes
@Serializable data object SplashRoute
@Serializable data object HomeRoute
@Serializable data class PlayerRoute(val trackId: Long)
@Serializable data class AlbumRoute(val collectionId: Long)

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val container = LocalAppContainer.current

    // Bottom sheet state — shared across screens
    var bottomSheetSong by remember { mutableStateOf<Song?>(null) }

    // Hide mini player on splash and player screens
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showMiniPlayer = currentDestination != null &&
        !currentDestination.hasRoute<PlayerRoute>() &&
        !currentDestination.hasRoute<SplashRoute>()

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            // Add bottom padding when mini player is visible to avoid content overlap
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<SplashRoute> {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(HomeRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    },
                )
            }

            composable<HomeRoute> {
                HomeScreen(
                    onNavigateToPlayer = { trackId ->
                        navController.navigate(PlayerRoute(trackId))
                    },
                    onShowBottomSheet = { song -> bottomSheetSong = song },
                )
            }

            composable<PlayerRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PlayerRoute>()
                PlayerScreen(
                    trackId = route.trackId,
                    onBack = { navController.popBackStack() },
                    onMoreClick = { song: Song -> bottomSheetSong = song },
                )
            }

            composable<AlbumRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AlbumRoute>()
                AlbumScreen(
                    collectionId = route.collectionId,
                    onBack = { navController.popBackStack() },
                    onTrackClick = { trackId ->
                        navController.navigate(PlayerRoute(trackId))
                    },
                )
            }
        }

        // Mini player — above NavHost, bottom-aligned, hidden on Player/Splash
        MiniPlayer(
            nowPlaying = container.nowPlaying,
            audioPlayer = container.audioPlayer,
            visible = showMiniPlayer,
            onClick = { trackId ->
                navController.navigate(PlayerRoute(trackId))
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    // Bottom sheet — rendered above everything
    bottomSheetSong?.let { song ->
        fun dismissSheet() {}
        SongBottomSheet(
            song = song,
            onDismiss = ::dismissSheet,
            onViewAlbum = {
                dismissSheet()
                navController.navigate(AlbumRoute(song.collectionId))
            },
        )
    }
}
