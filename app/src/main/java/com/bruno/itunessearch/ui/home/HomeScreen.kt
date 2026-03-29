package com.bruno.itunessearch.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bruno.itunessearch.R
import com.bruno.itunessearch.di.LocalAppContainer
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.UiError
import com.bruno.itunessearch.ui.components.StickySearchBar
import com.bruno.itunessearch.ui.components.SongListItem
import com.bruno.itunessearch.ui.toStringRes

@Composable
fun HomeScreen(
    onNavigateToPlayer: (trackId: Long) -> Unit,
    onShowBottomSheet: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(container.songRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        onEvent = viewModel::onEvent,
        onSongClick = { song ->
            container.nowPlaying.setPlaylist(state.displayedSongs)
            onNavigateToPlayer(song.trackId)
        },
        onMoreClick = onShowBottomSheet,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    onSongClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Back dismisses keyboard first, then default system back
    BackHandler(enabled = state.searchQuery.isNotBlank()) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    val errorMessage = state.error?.let { stringResource(it.toStringRes()) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null && state.displayedSongs.isNotEmpty()) {
            snackbarHostState.showSnackbar(message = errorMessage)
            onEvent(HomeEvent.ErrorDismissed)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(HomeEvent.PullToRefresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // Title — scrolls away
                item(key = "title") {
                    Text(
                        text = stringResource(R.string.songs_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                // Search bar — sticks to top when title scrolls away
                stickyHeader(key = "search") {
                    StickySearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                    )
                }

                if (state.isLoading) {
                    loadingSection()
                }

                if (state.error != null && state.displayedSongs.isEmpty()) {
                    errorSection(
                        error = state.error,
                        onRetry = { onEvent(HomeEvent.Retry) },
                    )
                }

                if (state.showEmptyState) {
                    emptySection(hasQuery = state.searchQuery.isNotBlank())
                }

                songListSection(
                    songs = state.displayedSongs,
                    onSongClick = onSongClick,
                    onMoreClick = onMoreClick,
                    onDismiss = if (state.isShowingRecentlyPlayed) {
                        { song -> onEvent(HomeEvent.DismissSong(song.trackId)) }
                    } else null,
                )
            }
        }
    }
}

// -- LazyList sections --

private fun LazyListScope.loadingSection() {
    item(key = "loading") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun LazyListScope.errorSection(
    error: UiError,
    onRetry: () -> Unit,
) {
    item(key = "error") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(error.toStringRes()),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.retry),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun LazyListScope.emptySection(hasQuery: Boolean) {
    item(key = "empty") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(
                    if (hasQuery) R.string.no_results else R.string.search_for_songs
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LazyListScope.songListSection(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    onDismiss: ((Song) -> Unit)?,
) {
    items(
        items = songs,
        key = { it.trackId },
    ) { song ->
        SongListItem(
            song = song,
            onSongClick = { onSongClick(song) },
            onMoreClick = { onMoreClick(song) },
            onDismiss = onDismiss?.let { { it(song) } },
        )
    }
}
