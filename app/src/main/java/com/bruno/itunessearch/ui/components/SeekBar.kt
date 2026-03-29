package com.bruno.itunessearch.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bruno.itunessearch.ui.theme.SeekBarProgress
import com.bruno.itunessearch.ui.theme.SeekBarTrack
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Single source of truth for slider position (0f..1f)
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var isUserDragging by remember { mutableStateOf(false) }

    // Sync slider from player position — only when user is NOT dragging
    LaunchedEffect(positionMs, durationMs) {
        if (!isUserDragging && durationMs > 0) {
            sliderValue = positionMs.toFloat() / durationMs.toFloat()
        }
    }

    val elapsedMs = (sliderValue * durationMs).toLong()
    val remainingMs = durationMs - elapsedMs

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderValue,
            onValueChange = { value ->
                isUserDragging = true
                sliderValue = value
            },
            onValueChangeFinished = {
                onSeek((sliderValue * durationMs).toLong())
                isUserDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = SeekBarProgress,
                activeTrackColor = SeekBarProgress,
                inactiveTrackColor = SeekBarTrack,
            ),
            thumb = {
                // Small circle thumb matching Figma (default is too large)
                Canvas(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape),
                ) {
                    drawCircle(color = SeekBarProgress)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(elapsedMs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "-${formatTime(remainingMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
