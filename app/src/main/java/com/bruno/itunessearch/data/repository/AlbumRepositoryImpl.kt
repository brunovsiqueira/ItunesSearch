package com.bruno.itunessearch.data.repository

import com.bruno.itunessearch.data.local.AlbumDao
import com.bruno.itunessearch.data.local.SongDao
import com.bruno.itunessearch.data.remote.ITunesApi
import com.bruno.itunessearch.data.remote.dto.toAlbum
import com.bruno.itunessearch.data.remote.dto.toAlbumEntity
import com.bruno.itunessearch.data.remote.dto.toSong
import com.bruno.itunessearch.data.remote.dto.toSongEntity
import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Album
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.domain.repository.AlbumRepository
import com.bruno.itunessearch.domain.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AlbumRepositoryImpl(
    private val api: ITunesApi,
    private val albumDao: AlbumDao,
    private val songDao: SongDao,
) : AlbumRepository {

    override fun getAlbumStream(collectionId: Long): Flow<Album?> =
        albumDao.getById(collectionId).map { it?.toAlbum() }

    override fun getAlbumTracksStream(collectionId: Long): Flow<List<Song>> =
        songDao.getByAlbum(collectionId).map { entities -> entities.map { it.toSong() } }

    override suspend fun fetchAlbumTracks(collectionId: Long): Result<Unit> =
        safeApiCall(tag = TAG) {
            val response = api.lookupAlbumTracks(collectionId)
            val tracks = response.results
            // First result is the collection itself (wrapperType == "collection"), rest are tracks
            val albumDto = tracks.firstOrNull { it.wrapperType == "collection" }
            albumDto?.toAlbumEntity()?.let { albumDao.insert(it) }
            val songEntities = tracks.mapNotNull { it.toSongEntity() }
            songDao.insertIfNotExists(songEntities)
        }

    companion object {
        private const val TAG = "AlbumRepository"
    }
}
