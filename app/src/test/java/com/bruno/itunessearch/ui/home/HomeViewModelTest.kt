package com.bruno.itunessearch.ui.home

import com.bruno.itunessearch.domain.AppLogger
import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.fakes.FakeSongRepository
import com.bruno.itunessearch.fakes.NoOpLogWriter
import com.bruno.itunessearch.fakes.TestData
import com.bruno.itunessearch.ui.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSongRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        AppLogger.writer = NoOpLogWriter()
        repository = FakeSongRepository()
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty with no loading`() {
        val state = viewModel.state.value
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
        assertTrue(state.recentlyPlayed.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `recently played songs are observed`() = runTest {
        val songs = TestData.sampleSongs
        repository.emitRecentlyPlayed(songs)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(songs, state.recentlyPlayed)
        assertEquals(songs, state.displayedSongs)
    }

    @Test
    fun `search triggers loading after debounce`() = runTest {
        viewModel.onEvent(HomeEvent.SearchQueryChanged("daft"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Query updated immediately
        assertEquals("daft", viewModel.state.value.searchQuery)

        // Advance past debounce
        advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        // After fetch completes, loading should be false
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `search results are displayed when query is active`() = runTest {
        val songs = TestData.sampleSongs

        viewModel.onEvent(HomeEvent.SearchQueryChanged("Test"))
        advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        // Results arrive from Room after fetch writes them
        repository.emitSearchResults(songs)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.searchResults.isEmpty())
    }

    @Test
    fun `network error sets error state`() = runTest {
        repository.fetchResult = Result.Failure.Network

        viewModel.onEvent(HomeEvent.SearchQueryChanged("test"))
        advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(UiError.Network, state.error)
        assertFalse(state.isLoading)
        assertTrue(state.hasSearched)
    }

    @Test
    fun `error dismissed clears error`() = runTest {
        repository.fetchResult = Result.Failure.Network
        viewModel.onEvent(HomeEvent.SearchQueryChanged("test"))
        advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(HomeEvent.ErrorDismissed)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `clearing search shows recently played`() = runTest {
        val recent = listOf(TestData.song(trackId = 99, trackName = "Recent"))
        repository.emitRecentlyPlayed(recent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Search something
        viewModel.onEvent(HomeEvent.SearchQueryChanged("test"))
        advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        // Clear search
        viewModel.onEvent(HomeEvent.SearchQueryChanged(""))
        advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isShowingRecentlyPlayed)
        assertEquals(recent, state.displayedSongs)
    }

    @Test
    fun `empty state not shown while search is pending`() = runTest {
        viewModel.onEvent(HomeEvent.SearchQueryChanged("test"))
        // Don't advance past debounce — search hasn't fired yet
        // hasSearched is false, so empty state should not show
        assertFalse(viewModel.state.value.hasSearched)
        assertFalse(viewModel.state.value.showEmptyState)
    }

    @Test
    fun `empty state shown after search with no results`() = runTest {
        viewModel.onEvent(HomeEvent.SearchQueryChanged("xyznoexist"))
        advanceTimeBy(400)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.hasSearched)
        assertTrue(state.showEmptyState)
    }
}
