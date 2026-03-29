package com.bruno.itunessearch.fakes

import com.bruno.itunessearch.domain.Result
import com.bruno.itunessearch.domain.model.Album
import com.bruno.itunessearch.domain.model.Song
import com.bruno.itunessearch.domain.repository.AlbumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAlbumRepository : AlbumRepository {

    private val album = MutableStateFlow<Album?>(null)
    private val tracks = MutableStateFlow<List<Song>>(emptyList())

    var fetchResult: Result<Unit> = Result.Success(Unit)

    fun emitAlbum(value: Album) {
        album.value = value
    }

    fun emitTracks(value: List<Song>) {
        tracks.value = value
    }

    override fun getAlbumStream(collectionId: Long): Flow<Album?> = album

    override fun getAlbumTracksStream(collectionId: Long): Flow<List<Song>> = tracks

    override suspend fun fetchAlbumTracks(collectionId: Long): Result<Unit> = fetchResult
}
