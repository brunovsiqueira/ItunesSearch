package com.bruno.itunessearch.ui.home

import androidx.compose.runtime.Immutable
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.UiError

@Immutable
data class HomeState(
    val searchQuery: String = "",
    val searchResults: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
    val isRefreshing: Boolean = false,
    val hasSearched: Boolean = false,
) {
    val displayedSongs: List<Song>
        get() = when {
            searchQuery.isNotBlank() -> searchResults
            recentlyPlayed.isNotEmpty() -> recentlyPlayed
            else -> searchResults
        }

    val isShowingRecentlyPlayed: Boolean
        get() = searchQuery.isBlank() && recentlyPlayed.isNotEmpty()

    val showEmptyState: Boolean
        get() = !isLoading && displayedSongs.isEmpty() && error == null &&
            (searchQuery.isBlank() || hasSearched)
}

sealed interface HomeEvent {
    data class SearchQueryChanged(val query: String) : HomeEvent
    data class SongClicked(val song: Song) : HomeEvent
    data object PullToRefresh : HomeEvent
    data object Retry : HomeEvent
    data object ErrorDismissed : HomeEvent
    data class DismissSong(val trackId: Long) : HomeEvent
}
