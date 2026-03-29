package com.bruno.itunessearch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.album.AlbumScreen
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

    // Bottom sheet state — shared across screens
    var bottomSheetSong by remember { mutableStateOf<Song?>(null) }

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier,
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
                onNavigateToAlbum = { collectionId ->
                    navController.navigate(AlbumRoute(collectionId))
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

    // Bottom sheet — rendered above NavHost, shared across all screens
    bottomSheetSong?.let { song ->
        SongBottomSheet(
            song = song,
            onDismiss = { bottomSheetSong = null },
            onViewAlbum = {
                bottomSheetSong = null
                navController.navigate(AlbumRoute(song.collectionId))
            },
        )
    }
}
