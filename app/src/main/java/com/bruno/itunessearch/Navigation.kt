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
            // TODO: Batch 2 — HomeScreen
            PlaceholderScreen("Home")
        }

        composable<PlayerRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PlayerRoute>()
            // TODO: Batch 3 — PlayerScreen(trackId = route.trackId)
            PlaceholderScreen("Player: ${route.trackId}")
        }

        composable<AlbumRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AlbumRoute>()
            // TODO: Batch 4 — AlbumScreen(collectionId = route.collectionId)
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
