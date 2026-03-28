package com.bruno.itunessearch.domain.repository

import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {

    fun getRecentlyPlayedStream(): Flow<List<Song>>

    fun getSearchResultsStream(query: String): Flow<List<Song>>

    suspend fun fetchSearchResults(query: String): Result<Unit>

    suspend fun getSongById(trackId: Long): Song?

    fun getSongsByAlbumStream(collectionId: Long): Flow<List<Song>>

    suspend fun markAsPlayed(trackId: Long)
}
