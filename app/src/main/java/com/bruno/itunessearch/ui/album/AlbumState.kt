package com.bruno.itunessearch.ui.album

import androidx.compose.runtime.Immutable
import com.bruno.itunessearch.domain.model.Album
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.UiError

@Immutable
data class AlbumState(
    val album: Album? = null,
    val tracks: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiError? = null,
)

sealed interface AlbumEvent {
    data object Retry : AlbumEvent
    data object ErrorDismissed : AlbumEvent
    data class TrackClicked(val song: Song) : AlbumEvent
}
