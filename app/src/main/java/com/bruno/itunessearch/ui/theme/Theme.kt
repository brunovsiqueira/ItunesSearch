package com.bruno.itunessearch.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = Black,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = GrayText,
    outline = GrayButtonBorder,
    surfaceContainerHighest = DarkSurfaceVariant,
)

@Composable
fun ItunesSearchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
