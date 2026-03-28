package com.bruno.itunessearch.domain.repository

import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Album
import com.bruno.itunessearch.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {

    fun getAlbumStream(collectionId: Long): Flow<Album?>

    fun getAlbumTracksStream(collectionId: Long): Flow<List<Song>>

    suspend fun fetchAlbumTracks(collectionId: Long): Result<Unit>
}
