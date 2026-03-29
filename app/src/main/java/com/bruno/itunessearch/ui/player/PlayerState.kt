package com.bruno.itunessearch.ui.player

import androidx.compose.runtime.Immutable
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.UiError

@Immutable
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playlist: List<Song> = emptyList(),
    val isRepeatEnabled: Boolean = false,
    val error: UiError? = null,
) {
    val hasPrevious: Boolean
        get() {
            val index = playlist.indexOfFirst { it.trackId == currentSong?.trackId }
            return index > 0
        }

    val hasNext: Boolean
        get() {
            val index = playlist.indexOfFirst { it.trackId == currentSong?.trackId }
            return index in 0 until playlist.lastIndex
        }
}

sealed interface PlayerEvent {
    data object PlayPause : PlayerEvent
    data object Next : PlayerEvent
    data object Previous : PlayerEvent
    data class SeekTo(val positionMs: Long) : PlayerEvent
    data object ToggleRepeat : PlayerEvent
    data object ErrorDismissed : PlayerEvent
}
