package com.bruno.itunessearch.data.repository

import com.bruno.itunessearch.data.local.SongDao
import com.bruno.itunessearch.data.remote.ITunesApi
import com.bruno.itunessearch.data.remote.dto.toSong
import com.bruno.itunessearch.data.remote.dto.toSongEntity
import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.domain.repository.SongRepository
import com.bruno.itunessearch.domain.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongRepositoryImpl(
    private val api: ITunesApi,
    private val songDao: SongDao,
) : SongRepository {

    override fun getRecentlyPlayedStream(): Flow<List<Song>> =
        songDao.getRecentlyPlayed().map { entities -> entities.map { it.toSong() } }

    override fun getSearchResultsStream(query: String): Flow<List<Song>> =
        songDao.getBySearchQuery(query).map { entities -> entities.map { it.toSong() } }

    override suspend fun fetchSearchResults(query: String): Result<Unit> =
        safeApiCall {
            val response = api.searchSongs(term = query)
            val entities = response.results.mapNotNull { it.toSongEntity(searchQuery = query) }
            songDao.clearByQuery(query)
            songDao.insertAll(entities)
        }

    override suspend fun getSongById(trackId: Long): Song? =
        songDao.getById(trackId)?.toSong()

    override fun getSongsByAlbumStream(collectionId: Long): Flow<List<Song>> =
        songDao.getByAlbum(collectionId).map { entities -> entities.map { it.toSong() } }

    override suspend fun markAsPlayed(trackId: Long) {
        songDao.updateLastPlayed(trackId)
    }
}
