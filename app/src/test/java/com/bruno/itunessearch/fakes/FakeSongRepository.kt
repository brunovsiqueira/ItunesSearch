package com.bruno.itunessearch.fakes

import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSongRepository : SongRepository {

    private val songs = MutableStateFlow<List<Song>>(emptyList())
    private val recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())

    var fetchResult: Result<Unit> = Result.Success(Unit)
    var songById: Song? = null
    val playedTrackIds = mutableListOf<Long>()

    fun emitSearchResults(results: List<Song>) {
        songs.value = results
    }

    fun emitRecentlyPlayed(results: List<Song>) {
        recentlyPlayed.value = results
    }

    override fun getRecentlyPlayedStream(): Flow<List<Song>> = recentlyPlayed

    override fun getSearchResultsStream(query: String): Flow<List<Song>> = songs

    override suspend fun fetchSearchResults(query: String): Result<Unit> = fetchResult

    override suspend fun getSongById(trackId: Long): Song? = songById

    override fun getSongsByAlbumStream(collectionId: Long): Flow<List<Song>> =
        songs.map { list -> list.filter { it.collectionId == collectionId } }

    override suspend fun markAsPlayed(trackId: Long) {
        playedTrackIds.add(trackId)
    }

    override suspend fun removeFromRecentlyPlayed(trackId: Long) {
        recentlyPlayed.value = recentlyPlayed.value.filter { it.trackId != trackId }
    }
}
