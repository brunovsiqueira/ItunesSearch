package com.bruno.itunessearch.player

import com.bruno.itunessearch.domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the currently playing song metadata.
 * Shared singleton — written by PlayerViewModel, read by MiniPlayer.
 * Separate from AudioPlayer to keep AudioPlayer domain-agnostic (it doesn't know about Song).
 */
class NowPlayingState {
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    fun setSong(song: Song) {
        _currentSong.value = song
    }
}
