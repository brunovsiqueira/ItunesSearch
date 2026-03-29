package com.bruno.itunessearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bruno.itunessearch.di.LocalAppContainer
import com.bruno.itunessearch.ui.components.OfflineBanner
import com.bruno.itunessearch.ui.theme.ItunesSearchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = (application as App).container
            val isConnected by container.connectivityObserver.isConnected
                .collectAsStateWithLifecycle(initialValue = true)

            CompositionLocalProvider(LocalAppContainer provides container) {
                ItunesSearchTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppNavHost()
                            OfflineBanner(
                                isOffline = !isConnected,
                                modifier = Modifier.align(Alignment.TopCenter),
                            )
                        }
                    }
                }
            }
        }
    }
}
