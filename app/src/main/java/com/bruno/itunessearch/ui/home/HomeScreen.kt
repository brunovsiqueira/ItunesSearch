package com.bruno.itunessearch.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bruno.itunessearch.R
import com.bruno.itunessearch.di.LocalAppContainer
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.ui.components.CollapsingHeader
import com.bruno.itunessearch.ui.components.SongListItem
import com.bruno.itunessearch.ui.toStringRes

/**
 * Entry point for the Home screen.
 * Creates its own ViewModel via LocalAppContainer — NavHost stays thin.
 * Navigation is handled via callbacks (ViewModel doesn't know about navigation).
 */
@Composable
fun HomeScreen(
    onNavigateToPlayer: (trackId: Long) -> Unit,
    onNavigateToAlbum: (collectionId: Long) -> Unit,
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
        onSongClick = { song -> onNavigateToPlayer(song.trackId) },
        onMoreClick = onShowBottomSheet,
        modifier = modifier,
    )
}

/**
 * Stateless content composable — receives state, emits events.
 * Separated from HomeScreen for testability and @Preview support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    onSongClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Collapse header when scrolled past first item
    val collapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    // Show error as snackbar — UI layer resolves domain error → string resource
    LaunchedEffect(state.error) {
        state.error?.let { failure ->
            snackbarHostState.showSnackbar(message = context.getString(failure.toStringRes()))
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
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // Header (collapsing search bar + title)
                item(key = "header") {
                    Spacer(modifier = Modifier.height(8.dp))
                    CollapsingHeader(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                        collapsed = collapsed,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Loading indicator
                if (state.isLoading) {
                    item(key = "loading") {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                // Empty state
                if (state.showEmptyState) {
                    item(key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(
                                    if (state.searchQuery.isBlank()) R.string.search_for_songs
                                    else R.string.no_results
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                // Song list
                items(
                    items = state.displayedSongs,
                    key = { it.trackId },
                ) { song ->
                    SongListItem(
                        song = song,
                        onSongClick = { onSongClick(song) },
                        onMoreClick = { onMoreClick(song) },
                    )
                }
            }
        }
    }
}
