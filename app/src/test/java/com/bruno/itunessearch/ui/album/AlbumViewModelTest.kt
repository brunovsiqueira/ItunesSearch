package com.bruno.itunessearch.ui.album

import app.cash.turbine.test
import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.fakes.FakeAlbumRepository
import com.bruno.itunessearch.fakes.TestData
import com.bruno.itunessearch.ui.UiError
import com.bruno.itunessearch.domain.AppLogger
import com.bruno.itunessearch.fakes.NoOpLogWriter
import com.bruno.itunessearch.player.NowPlayingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAlbumRepository
    private lateinit var nowPlaying: NowPlayingState
    private lateinit var viewModel: AlbumViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        AppLogger.writer = NoOpLogWriter()
        repository = FakeAlbumRepository()
        nowPlaying = NowPlayingState()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AlbumViewModel(
        collectionId = 100L,
        albumRepository = repository,
        nowPlaying = nowPlaying,
    )

    @Test
    fun `loads album and tracks on init`() = runTest {
        val album = TestData.album()
        val tracks = TestData.sampleSongs
        repository.emitAlbum(album)
        repository.emitTracks(tracks)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(album, state.album)
            assertEquals(tracks, state.tracks)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `network error sets error state`() = runTest {
        repository.fetchResult = Result.Failure.Network

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(UiError.Network, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `retry re-fetches album`() = runTest {
        repository.fetchResult = Result.Failure.Network
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        repository.fetchResult = Result.Success(Unit)
        viewModel.onEvent(AlbumEvent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `error dismissed clears error`() = runTest {
        repository.fetchResult = Result.Failure.Network
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AlbumEvent.ErrorDismissed)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `track clicked sets playlist in NowPlayingState`() = runTest {
        val tracks = TestData.sampleSongs
        repository.emitTracks(tracks)

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AlbumEvent.TrackClicked(tracks[0]))

        assertEquals(tracks, nowPlaying.playlist.value)
    }
}
