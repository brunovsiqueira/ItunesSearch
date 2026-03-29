package com.bruno.itunessearch.fakes

import com.bruno.itunessearch.domain.model.Album
import com.bruno.itunessearch.domain.model.Song

object TestData {

    fun song(
        trackId: Long = 1L,
        trackName: String = "Test Song",
        artistName: String = "Test Artist",
        collectionId: Long = 100L,
        collectionName: String = "Test Album",
        artworkUrl: String = "https://example.com/art.jpg",
        previewUrl: String? = "https://example.com/preview.m4a",
        trackTimeMillis: Long = 30_000L,
        trackNumber: Int = 1,
    ) = Song(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        collectionId = collectionId,
        collectionName = collectionName,
        artworkUrl = artworkUrl,
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis,
        trackNumber = trackNumber,
    )

    fun album(
        collectionId: Long = 100L,
        collectionName: String = "Test Album",
        artistName: String = "Test Artist",
        artworkUrl: String = "https://example.com/art.jpg",
        trackCount: Int = 10,
    ) = Album(
        collectionId = collectionId,
        collectionName = collectionName,
        artistName = artistName,
        artworkUrl = artworkUrl,
        trackCount = trackCount,
    )

    val sampleSongs = listOf(
        song(trackId = 1, trackName = "One More Time", trackNumber = 1),
        song(trackId = 2, trackName = "Aerodynamic", trackNumber = 2),
        song(trackId = 3, trackName = "Digital Love", trackNumber = 3),
    )
}
