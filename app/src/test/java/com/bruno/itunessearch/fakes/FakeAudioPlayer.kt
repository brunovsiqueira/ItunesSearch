package com.bruno.itunessearch.fakes

import com.bruno.itunessearch.player.AudioPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeAudioPlayer : AudioPlayer {
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(30_000L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    override val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    override val positionFlow: Flow<Long> = flowOf(0L)

    var lastPlayedUrl: String? = null
        private set
    var playCount = 0
        private set

    override fun play(url: String) {
        lastPlayedUrl = url
        playCount++
        _isPlaying.value = true
    }

    override fun resume() {
        _isPlaying.value = true
    }

    override fun pause() {
        _isPlaying.value = false
    }

    override fun seekTo(positionMs: Long) {}

    override fun release() {}

    override fun clearError() {
        _hasError.value = false
    }
}
