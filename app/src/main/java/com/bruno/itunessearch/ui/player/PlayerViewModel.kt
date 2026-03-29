package com.bruno.itunessearch.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.domain.repository.SongRepository
import com.bruno.itunessearch.player.AudioPlayer
import com.bruno.itunessearch.player.NowPlayingState
import com.bruno.itunessearch.ui.UiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val trackId: Long,
    private val songRepository: SongRepository,
    private val audioPlayer: AudioPlayer,
    private val nowPlaying: NowPlayingState,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    init {
        loadSong()
        observePlaybackState()
    }

    fun onEvent(event: PlayerEvent) {
        when (event) {
            is PlayerEvent.PlayPause -> togglePlayPause()
            is PlayerEvent.Next -> skipNext()
            is PlayerEvent.Previous -> skipPrevious()
            is PlayerEvent.SeekTo -> audioPlayer.seekTo(event.positionMs)
            is PlayerEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
                audioPlayer.clearError()
            }
        }
    }

    private fun loadSong() {
        viewModelScope.launch {
            val song = songRepository.getSongById(trackId) ?: return@launch
            _state.update { it.copy(currentSong = song) }
            songRepository.markAsPlayed(trackId)

            // If this song is already the current one, just sync state — don't restart playback
            if (nowPlaying.currentSong.value?.trackId == trackId) {
                nowPlaying.setSong(song)
                return@launch
            }

            nowPlaying.setSong(song)
            if (song.previewUrl != null) {
                audioPlayer.play(song.previewUrl)
            } else {
                _state.update { it.copy(error = UiError.NoPreview) }
            }
        }

        // Load playlist from same album for next/prev navigation
        viewModelScope.launch {
            val song = songRepository.getSongById(trackId) ?: return@launch
            songRepository.getSongsByAlbumStream(song.collectionId).collectLatest { songs ->
                _state.update { it.copy(playlist = songs) }
            }
        }
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            audioPlayer.isPlaying.collectLatest { playing ->
                _state.update { it.copy(isPlaying = playing) }
            }
        }
        viewModelScope.launch {
            audioPlayer.duration.collectLatest { duration ->
                _state.update { it.copy(durationMs = duration) }
            }
        }
        viewModelScope.launch {
            audioPlayer.positionFlow.collectLatest { position ->
                _state.update { it.copy(positionMs = position) }
            }
        }
        viewModelScope.launch {
            audioPlayer.hasError.collectLatest { hasError ->
                if (hasError) {
                    _state.update { it.copy(error = UiError.Network) }
                }
            }
        }
    }

    private fun togglePlayPause() {
        if (_state.value.isPlaying) {
            audioPlayer.pause()
        } else {
            audioPlayer.resume()
        }
    }

    private fun skipNext() {
        val current = _state.value.currentSong ?: return
        val playlist = _state.value.playlist
        val index = playlist.indexOfFirst { it.trackId == current.trackId }
        if (index < playlist.lastIndex) {
            playSong(playlist[index + 1])
        }
    }

    private fun skipPrevious() {
        val current = _state.value.currentSong ?: return
        val playlist = _state.value.playlist
        val index = playlist.indexOfFirst { it.trackId == current.trackId }
        if (index > 0) {
            playSong(playlist[index - 1])
        }
    }

    private fun playSong(song: Song) {
        _state.update { it.copy(currentSong = song, positionMs = 0L) }
        nowPlaying.setSong(song)
        viewModelScope.launch { songRepository.markAsPlayed(song.trackId) }
        song.previewUrl?.let { audioPlayer.play(it) }
    }
}
