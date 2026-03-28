package com.bruno.itunessearch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionId: Long,
    val collectionName: String,
    val artworkUrl: String,
    val previewUrl: String?,
    val trackTimeMillis: Long,
    val trackNumber: Int,
    val searchQuery: String?,
    val lastPlayedAt: Long? = null,
    val cachedAt: Long = System.currentTimeMillis(),
)
