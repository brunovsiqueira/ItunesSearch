package com.bruno.itunessearch.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>,
)
