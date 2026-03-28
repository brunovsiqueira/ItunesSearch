package com.bruno.itunessearch.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Album(
    val collectionId: Long,
    val collectionName: String,
    val artistName: String,
    val artworkUrl: String,
    val trackCount: Int,
)
