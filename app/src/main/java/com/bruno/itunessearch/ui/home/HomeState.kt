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
    /**
     * What to show in the song list:
     * - Active search → search results
     * - Has recently played → recently played
     * - First launch (nothing played yet) → fallback to any cached results
     */
    val displayedSongs: List<Song>
        get() = when {
            searchQuery.isNotBlank() -> searchResults
            recentlyPlayed.isNotEmpty() -> recentlyPlayed
            else -> searchResults
        }

    val isShowingRecentlyPlayed: Boolean
        get() = searchQuery.isBlank() && recentlyPlayed.isNotEmpty()

    /**
     * Show empty state only when:
     * - Not loading
     * - No error
     * - No songs to display
     * - AND either: no active search (show "Search for songs"), or search completed with no results
     */
    val showEmptyState: Boolean
        get() = !isLoading && displayedSongs.isEmpty() && error == null &&
            (searchQuery.isBlank() || hasSearched)
}

sealed interface HomeEvent {
    data class SearchQueryChanged(val query: String) : HomeEvent
    data class SongClicked(val song: Song) : HomeEvent
    data class MoreClicked(val song: Song) : HomeEvent
    data object PullToRefresh : HomeEvent
    data object Retry : HomeEvent
    data object ErrorDismissed : HomeEvent
    data class DismissSong(val trackId: Long) : HomeEvent
}
