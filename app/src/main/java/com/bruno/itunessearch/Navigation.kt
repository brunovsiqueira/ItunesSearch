package com.bruno.itunessearch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bruno.itunessearch.ui.home.HomeScreen
import com.bruno.itunessearch.ui.player.PlayerScreen
import kotlinx.serialization.Serializable

// Type-safe navigation routes
@Serializable data object SplashRoute
@Serializable data object HomeRoute
@Serializable data class PlayerRoute(val trackId: Long)
@Serializable data class AlbumRoute(val collectionId: Long)

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToPlayer = { trackId ->
                    navController.navigate(PlayerRoute(trackId))
                },
                onNavigateToAlbum = { collectionId ->
                    navController.navigate(AlbumRoute(collectionId))
                },
            )
        }

        composable<PlayerRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PlayerRoute>()
            PlayerScreen(
                trackId = route.trackId,
                onBack = { navController.popBackStack() },
                onMoreClick = { /* TODO: Batch 4 — bottom sheet */ },
            )
        }

        composable<AlbumRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AlbumRoute>()
            // TODO: Batch 4 — AlbumScreen
            PlaceholderScreen("Album: ${route.collectionId}")
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
