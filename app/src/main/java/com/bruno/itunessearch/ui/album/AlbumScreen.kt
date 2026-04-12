package com.bruno.itunessearch.ui.album

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bruno.itunessearch.di.LocalAppContainer
import com.bruno.itunessearch.ui.components.AlbumArtwork
import com.bruno.itunessearch.ui.components.ErrorState
import com.bruno.itunessearch.ui.components.ScreenTopBar
import com.bruno.itunessearch.ui.components.SongListItem

@Composable
fun AlbumScreen(
    collectionId: Long,
    onBack: () -> Unit,
    onTrackClick: (trackId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalAppContainer.current
    val viewModel: AlbumViewModel = viewModel {
        AlbumViewModel(
            collectionId = collectionId,
            albumRepository = container.albumRepository,
            nowPlaying = container.nowPlaying,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    AlbumContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onTrackClick = { song ->
            viewModel.onEvent(AlbumEvent.TrackClicked(song))
            onTrackClick(song.trackId)
        },
        modifier = modifier,
    )
}

@Composable
private fun AlbumContent(
    state: AlbumState,
    onEvent: (AlbumEvent) -> Unit,
    onBack: () -> Unit,
    onTrackClick: (com.bruno.itunessearch.domain.model.Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "topbar") {
                ScreenTopBar(
                    title = state.album?.collectionName.orEmpty(),
                    onBack = onBack,
                )
            }

            item(key = "album_header") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp),
                ) {
                    state.album?.let { album ->
                        AlbumArtwork(
                            artworkUrl = album.artworkUrl,
                            albumName = album.collectionName,
                            size = 200.dp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = album.collectionName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = album.artistName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            if (state.isLoading && state.tracks.isEmpty()) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (state.error != null && state.tracks.isEmpty()) {
                item(key = "error") {
                    ErrorState(
                        error = state.error,
                        onRetry = { onEvent(AlbumEvent.Retry) },
                    )
                }
            }

            items(items = state.tracks, key = { it.trackId }) { song ->
                SongListItem(
                    song = song,
                    onSongClick = { onTrackClick(song) },
                )
            }
        }
    }
}
