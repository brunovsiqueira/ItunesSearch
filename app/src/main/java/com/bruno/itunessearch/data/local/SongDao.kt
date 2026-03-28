package com.bruno.itunessearch.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bruno.itunessearch.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs WHERE searchQuery = :query ORDER BY trackName ASC")
    fun getBySearchQuery(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC")
    fun getRecentlyPlayed(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE trackId = :trackId")
    suspend fun getById(trackId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE collectionId = :collectionId ORDER BY trackNumber ASC")
    fun getByAlbum(collectionId: Long): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Query("UPDATE songs SET lastPlayedAt = :timestamp WHERE trackId = :trackId")
    suspend fun updateLastPlayed(trackId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM songs WHERE searchQuery = :query")
    suspend fun clearByQuery(query: String)
}
