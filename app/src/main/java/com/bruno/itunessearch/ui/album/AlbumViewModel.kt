package com.bruno.itunessearch.ui.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.domain.repository.AlbumRepository
import com.bruno.itunessearch.player.NowPlayingState
import com.bruno.itunessearch.ui.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlbumViewModel(
    private val collectionId: Long,
    private val albumRepository: AlbumRepository,
    private val nowPlaying: NowPlayingState,
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumState())
    val state: StateFlow<AlbumState> = _state.asStateFlow()

    init {
        observeAlbum()
        fetchAlbum()
    }

    fun onEvent(event: AlbumEvent) {
        when (event) {
            is AlbumEvent.Retry -> fetchAlbum()
            is AlbumEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
            is AlbumEvent.TrackClicked -> onTrackClicked(event.song)
        }
    }

    private fun onTrackClicked(song: Song) {
        nowPlaying.setPlaylist(_state.value.tracks)
    }

    private fun observeAlbum() {
        viewModelScope.launch {
            albumRepository.getAlbumStream(collectionId).collectLatest { album ->
                _state.update { it.copy(album = album) }
            }
        }
        viewModelScope.launch {
            albumRepository.getAlbumTracksStream(collectionId).collectLatest { tracks ->
                _state.update { it.copy(tracks = tracks) }
            }
        }
    }

    private fun fetchAlbum() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = albumRepository.fetchAlbumTracks(collectionId)) {
                is Result.Success -> _state.update { it.copy(isLoading = false) }
                is Result.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.toUiError())
                }
            }
        }
    }
}
