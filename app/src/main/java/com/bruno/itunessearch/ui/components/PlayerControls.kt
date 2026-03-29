package com.bruno.itunessearch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bruno.itunessearch.R
import com.bruno.itunessearch.ui.theme.GrayButton
import com.bruno.itunessearch.ui.theme.White

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    isRepeatEnabled: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Play/Pause — large circle
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GrayButton),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) R.string.pause else R.string.play
                ),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Previous — custom Figma icon
        IconButton(
            onClick = onPrevious,
            enabled = hasPrevious,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_skip_previous),
                contentDescription = stringResource(R.string.previous_track),
                tint = White.copy(alpha = if (hasPrevious) 1f else 0.4f),
                modifier = Modifier.size(36.dp),
            )
        }

        // Next — custom Figma icon
        IconButton(
            onClick = onNext,
            enabled = hasNext,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_skip_next),
                contentDescription = stringResource(R.string.next_track),
                tint = White.copy(alpha = if (hasNext) 1f else 0.4f),
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Repeat — custom Figma icon (22dp per design)
        IconButton(onClick = onToggleRepeat) {
            Icon(
                painter = painterResource(R.drawable.ic_repeat),
                contentDescription = stringResource(R.string.repeat),
                tint = if (isRepeatEnabled) White else White.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
