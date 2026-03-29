package com.bruno.itunessearch.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.repository.SongRepository
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
            is HomeEvent.SongClicked -> { /* handled by screen via navigation callback */ }
            is HomeEvent.MoreClicked -> { /* handled by screen via bottom sheet callback */ }
            is HomeEvent.PullToRefresh -> refresh()
            is HomeEvent.Retry -> retry()
            is HomeEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            songRepository.getRecentlyPlayedStream().collectLatest { songs ->
                _state.update { it.copy(recentlyPlayed = songs) }
            }
        }
    }

    private fun observeSearchQuery() {
        // Trigger API fetch on query change (debounced)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _state.update { it.copy(searchResults = emptyList(), isLoading = false) }
                        return@collectLatest
                    }
                    search(query)
                }
        }

        // Observe search results from Room (reactive — auto-emits when Room is written to)
        viewModelScope.launch {
            searchQueryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) return@collectLatest
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
            is Result.Failure -> _state.update { it.copy(isLoading = false, hasSearched = true, error = result.toUiError()) }
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

    private fun retry() {
        val query = _state.value.searchQuery
        if (query.isBlank()) return
        viewModelScope.launch { search(query) }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
