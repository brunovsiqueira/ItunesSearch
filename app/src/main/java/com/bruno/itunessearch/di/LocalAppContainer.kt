package com.bruno.itunessearch.di

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides AppContainer to the Compose tree via CompositionLocal.
 * Screens access it to create their own ViewModels with dependencies.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided. Wrap your content with CompositionLocalProvider.")
}
