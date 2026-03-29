package com.bruno.itunessearch.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bruno.itunessearch.R
import com.bruno.itunessearch.ui.theme.Black
import com.bruno.itunessearch.ui.theme.SplashGradientCenter
import com.bruno.itunessearch.ui.theme.SplashGradientMid
import kotlinx.coroutines.delay

private val SplashGradient = Brush.radialGradient(
    colors = listOf(SplashGradientCenter, SplashGradientMid, Black),
)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SplashGradient),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_splash_music_note),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
        )
    }
}

private const val SPLASH_DURATION_MS = 1500L
