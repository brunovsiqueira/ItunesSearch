package com.bruno.itunessearch.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bruno.itunessearch.domain.AppLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

/**
 * Wraps Media3 ExoPlayer. Exposes playback state as reactive flows.
 * The ViewModel observes these flows — the UI never touches ExoPlayer directly.
 * All ExoPlayer operations are wrapped to catch and log errors.
 */
class AudioPlayer(context: Context) {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    /**
     * Emits current position every [POSITION_UPDATE_INTERVAL_MS].
     * Cold flow — only active while collected.
     */
    val positionFlow: Flow<Long> = flow {
        while (true) {
            emit(exoPlayer.currentPosition)
            delay(POSITION_UPDATE_INTERVAL_MS)
        }
    }

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.update { isPlaying }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.update { exoPlayer.duration.coerceAtLeast(0L) }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                AppLogger.error(TAG, "Playback error: ${error.errorCodeName}", error)
                _hasError.update { true }
                _isPlaying.update { false }
            }
        })
    }

    fun play(url: String) {
        runCatching {
            _hasError.update { false }
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            exoPlayer.play()
        }.onFailure { e ->
            AppLogger.error(TAG, "Failed to start playback", e)
            _hasError.update { true }
        }
    }

    fun resume() {
        runCatching { exoPlayer.play() }
            .onFailure { e -> AppLogger.error(TAG, "Failed to resume", e) }
    }

    fun pause() {
        runCatching { exoPlayer.pause() }
            .onFailure { e -> AppLogger.error(TAG, "Failed to pause", e) }
    }

    fun seekTo(positionMs: Long) {
        runCatching { exoPlayer.seekTo(positionMs) }
            .onFailure { e -> AppLogger.error(TAG, "Failed to seek", e) }
    }

    fun release() {
        runCatching { exoPlayer.release() }
            .onFailure { e -> AppLogger.error(TAG, "Failed to release", e) }
    }

    fun clearError() {
        _hasError.update { false }
    }

    companion object {
        private const val TAG = "AudioPlayer"
        private const val POSITION_UPDATE_INTERVAL_MS = 200L
    }
}
