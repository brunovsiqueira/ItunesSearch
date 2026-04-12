package com.bruno.itunessearch.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.domain.repository.SongRepository
import com.bruno.itunessearch.player.NowPlayingState
import com.bruno.itunessearch.ui.toUiError
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val songRepository: SongRepository,
    private val nowPlaying: NowPlayingState,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        observeRecentlyPlayed()
        observeSearchQuery()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query, hasSearched = false) }
                searchQueryFlow.value = event.query
            }
            is HomeEvent.SongClicked -> onSongClicked(event.song)
            is HomeEvent.PullToRefresh -> refresh()
            is HomeEvent.Retry -> retry()
            is HomeEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
            is HomeEvent.DismissSong -> dismissSong(event.trackId)
        }
    }

    private fun onSongClicked(song: Song) {
        // Set playlist context before navigation — ViewModel owns this responsibility
        nowPlaying.setPlaylist(_state.value.displayedSongs)
    }

    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            songRepository.getRecentlyPlayedStream().collectLatest { songs ->
                _state.update { it.copy(recentlyPlayed = songs) }
            }
        }
    }

    /**
     * Single coroutine handles both API fetch and Room observation for the current query.
     * collectLatest cancels the previous collection when query changes, preventing race conditions.
     */
    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _state.update { it.copy(searchResults = emptyList(), isLoading = false) }
                        return@collectLatest
                    }

                    // Fetch from API (writes to Room)
                    search(query)

                    // Then observe Room for reactive updates (e.g., if cache is populated)
                    songRepository.getSearchResultsStream(query).collectLatest { songs ->
                        _state.update { it.copy(searchResults = songs) }
                    }
                }
        }
    }

    private suspend fun search(query: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        when (val result = songRepository.fetchSearchResults(query)) {
            is Result.Success -> _state.update { it.copy(isLoading = false, hasSearched = true) }
            is Result.Failure -> _state.update {
                it.copy(isLoading = false, hasSearched = true, error = result.toUiError())
            }
        }
    }

    private fun refresh() {
        val query = _state.value.searchQuery
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            songRepository.fetchSearchResults(query)
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun dismissSong(trackId: Long) {
        viewModelScope.launch { songRepository.removeFromRecentlyPlayed(trackId) }
    }

    private fun retry() {
        val query = _state.value.searchQuery
        if (query.isBlank()) return
        viewModelScope.launch { search(query) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
