package com.bruno.itunessearch.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Song(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val collectionId: Long,
    val collectionName: String,
    val artworkUrl: String,
    val previewUrl: String?,
    val trackTimeMillis: Long,
    val trackNumber: Int,
)
