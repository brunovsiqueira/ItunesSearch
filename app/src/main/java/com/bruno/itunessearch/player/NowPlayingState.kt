package com.bruno.itunessearch.player

import com.bruno.itunessearch.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the currently playing song and its playlist context.
 * Shared singleton — the launching screen sets the playlist,
 * PlayerViewModel reads it for next/prev navigation.
 */
class NowPlayingState {
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    fun setSong(song: Song) {
        _currentSong.value = song
    }

    /**
     * Called by the launching screen (Home or Album) to set the playlist context.
     * Forward/backward in the player navigates within this list.
     */
    fun setPlaylist(songs: List<Song>) {
        _playlist.value = songs
    }
}
