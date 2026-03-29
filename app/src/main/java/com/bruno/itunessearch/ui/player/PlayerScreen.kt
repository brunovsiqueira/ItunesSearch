package com.bruno.itunessearch.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
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
import com.bruno.itunessearch.player.AudioPlayer
import com.bruno.itunessearch.ui.components.AlbumArtwork
import com.bruno.itunessearch.ui.components.CircleIconButton
import com.bruno.itunessearch.ui.components.PlayerControls
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
            audioPlayer = AudioPlayer(container.applicationContext),
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
            // Top bar: back + album title + more
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    onClick = onBack,
                )
                Text(
                    text = song?.collectionName.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                CircleIconButton(
                    icon = Icons.Default.MoreHoriz,
                    contentDescription = stringResource(R.string.more_options),
                    onClick = onMoreClick,
                )
            }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = stringResource(R.string.repeat),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
