package com.bruno.itunessearch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val collectionId: Long,
    val collectionName: String,
    val artistName: String,
    val artworkUrl: String,
    val trackCount: Int,
    val cachedAt: Long = System.currentTimeMillis(),
)
