package com.bruno.itunessearch.ui.player

import com.bruno.itunessearch.domain.AppLogger
import com.bruno.itunessearch.fakes.FakeAudioPlayer
import com.bruno.itunessearch.fakes.FakeSongRepository
import com.bruno.itunessearch.fakes.NoOpLogWriter
import com.bruno.itunessearch.fakes.TestData
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSongRepository
    private lateinit var audioPlayer: FakeAudioPlayer
    private lateinit var nowPlaying: NowPlayingState

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        AppLogger.writer = NoOpLogWriter()
        repository = FakeSongRepository()
        audioPlayer = FakeAudioPlayer()
        nowPlaying = NowPlayingState()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(trackId: Long = 1L) = PlayerViewModel(
        trackId = trackId,
        songRepository = repository,
        audioPlayer = audioPlayer,
        nowPlaying = nowPlaying,
    )

    @Test
    fun `loads song and starts playback on init`() = runTest {
        val song = TestData.song(trackId = 1, previewUrl = "https://example.com/preview.m4a")
        repository.songById = song

        val viewModel = createViewModel(trackId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(song, state.currentSong)
        assertEquals(1, audioPlayer.playCount)
        assertEquals("https://example.com/preview.m4a", audioPlayer.lastPlayedUrl)
    }

    @Test
    fun `sets now playing state on load`() = runTest {
        val song = TestData.song(trackId = 1)
        repository.songById = song

        createViewModel(trackId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(song, nowPlaying.currentSong.value)
    }

    @Test
    fun `marks song as played`() = runTest {
        val song = TestData.song(trackId = 42)
        repository.songById = song

        createViewModel(trackId = 42)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(42L in repository.playedTrackIds)
    }

    @Test
    fun `does not restart if same song is already playing`() = runTest {
        val song = TestData.song(trackId = 1)
        repository.songById = song
        nowPlaying.setSong(song) // already the current song

        createViewModel(trackId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // play() should NOT be called since the song is already current
        assertEquals(0, audioPlayer.playCount)
    }

    @Test
    fun `play pause toggles playback`() = runTest {
        val song = TestData.song(trackId = 1)
        repository.songById = song

        val viewModel = createViewModel(trackId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should be playing after init
        assertTrue(viewModel.state.value.isPlaying)

        // Pause
        viewModel.onEvent(PlayerEvent.PlayPause)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.state.value.isPlaying)

        // Resume
        viewModel.onEvent(PlayerEvent.PlayPause)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.isPlaying)
    }

    @Test
    fun `skip next plays next song in playlist`() = runTest {
        val songs = TestData.sampleSongs
        val firstSong = songs[0]
        repository.songById = firstSong
        repository.emitSearchResults(songs) // playlist comes from album stream

        val viewModel = createViewModel(trackId = firstSong.trackId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(PlayerEvent.Next)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(songs[1], viewModel.state.value.currentSong)
        assertEquals(2, audioPlayer.playCount) // initial + skip
    }

    @Test
    fun `skip previous plays previous song in playlist`() = runTest {
        val songs = TestData.sampleSongs
        val secondSong = songs[1]
        repository.songById = secondSong
        repository.emitSearchResults(songs)

        val viewModel = createViewModel(trackId = secondSong.trackId)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(PlayerEvent.Previous)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(songs[0], viewModel.state.value.currentSong)
    }

    @Test
    fun `no preview url sets error`() = runTest {
        val song = TestData.song(trackId = 1, previewUrl = null)
        repository.songById = song

        val viewModel = createViewModel(trackId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)
        assertEquals(0, audioPlayer.playCount)
    }

    @Test
    fun `error dismissed clears error`() = runTest {
        val song = TestData.song(trackId = 1, previewUrl = null)
        repository.songById = song

        val viewModel = createViewModel(trackId = 1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(PlayerEvent.ErrorDismissed)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `song not found does not crash`() = runTest {
        repository.songById = null

        val viewModel = createViewModel(trackId = 999)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.currentSong)
        assertEquals(0, audioPlayer.playCount)
    }
}
