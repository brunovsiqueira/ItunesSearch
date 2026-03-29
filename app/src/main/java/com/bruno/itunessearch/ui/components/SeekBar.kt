package com.bruno.itunessearch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.bruno.itunessearch.ui.theme.SeekBarProgress
import com.bruno.itunessearch.ui.theme.SeekBarTrack
import java.util.Locale

/**
 * Custom seek bar matching Figma: thin track (3dp), small circle thumb (10dp).
 * Drawn manually via Canvas for pixel-perfect control over track height and thumb size.
 */
@Composable
fun SeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var isUserDragging by remember { mutableStateOf(false) }

    LaunchedEffect(positionMs, durationMs) {
        if (!isUserDragging && durationMs > 0) {
            sliderValue = positionMs.toFloat() / durationMs.toFloat()
        }
    }

    val elapsedMs = (sliderValue * durationMs).toLong()
    val remainingMs = durationMs - elapsedMs

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp) // touch target height
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                        sliderValue = fraction
                        onSeek((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { isUserDragging = true },
                        onDragEnd = {
                            isUserDragging = false
                            onSeek((sliderValue * durationMs).toLong())
                        },
                        onDragCancel = { isUserDragging = false },
                        onHorizontalDrag = { _, dragAmount ->
                            val delta = dragAmount / size.width
                            sliderValue = (sliderValue + delta).coerceIn(0f, 1f)
                        },
                    )
                },
        ) {
            val trackY = size.height / 2
            val trackHeight = 3.dp.toPx()
            val thumbRadius = 5.dp.toPx()
            val thumbX = sliderValue * size.width

            // Inactive track (full width)
            drawLine(
                color = SeekBarTrack,
                start = Offset(0f, trackY),
                end = Offset(size.width, trackY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round,
            )

            // Active track (up to thumb)
            if (thumbX > 0) {
                drawLine(
                    color = SeekBarProgress,
                    start = Offset(0f, trackY),
                    end = Offset(thumbX, trackY),
                    strokeWidth = trackHeight,
                    cap = StrokeCap.Round,
                )
            }

            // Thumb circle
            drawCircle(
                color = SeekBarProgress,
                radius = thumbRadius,
                center = Offset(thumbX.coerceIn(thumbRadius, size.width - thumbRadius), trackY),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
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
