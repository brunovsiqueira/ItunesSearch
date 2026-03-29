package com.bruno.itunessearch.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bruno.itunessearch.R
import com.bruno.itunessearch.di.LocalAppContainer
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.components.AlbumArtwork
import com.bruno.itunessearch.ui.components.PlayerControls
import com.bruno.itunessearch.ui.components.ScreenTopBar
import com.bruno.itunessearch.ui.components.SeekBar

@Composable
fun PlayerScreen(
    trackId: Long,
    onBack: () -> Unit,
    onMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalAppContainer.current
    val viewModel: PlayerViewModel = viewModel {
        PlayerViewModel(
            trackId = trackId,
            songRepository = container.songRepository,
            audioPlayer = container.audioPlayer,
            nowPlaying = container.nowPlaying,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    PlayerContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onMoreClick = { state.currentSong?.let(onMoreClick) },
        modifier = modifier,
    )
}

@Composable
private fun PlayerContent(
    state: PlayerState,
    onEvent: (PlayerEvent) -> Unit,
    onBack: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScreenTopBar(
                title = stringResource(R.string.now_playing),
                onBack = onBack,
                trailingIcon = Icons.Default.MoreVert,
                onTrailingClick = onMoreClick,
                trailingContentDescription = stringResource(R.string.more_options),
            )

            Spacer(modifier = Modifier.weight(1f))

            // Album artwork
            if (song != null) {
                AlbumArtwork(
                    artworkUrl = song.artworkUrl,
                    albumName = song.collectionName,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Song info
            if (song != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = song.trackName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artistName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seek bar
            SeekBar(
                positionMs = state.positionMs,
                durationMs = state.durationMs,
                onSeek = { onEvent(PlayerEvent.SeekTo(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            PlayerControls(
                isPlaying = state.isPlaying,
                hasPrevious = state.hasPrevious,
                hasNext = state.hasNext,
                onPlayPause = { onEvent(PlayerEvent.PlayPause) },
                onPrevious = { onEvent(PlayerEvent.Previous) },
                onNext = { onEvent(PlayerEvent.Next) },
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
