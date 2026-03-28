package com.bruno.itunessearch.data.remote.dto

import com.bruno.itunessearch.data.local.entity.AlbumEntity
import com.bruno.itunessearch.data.local.entity.SongEntity
import com.bruno.itunessearch.domain.model.Album
import com.bruno.itunessearch.domain.model.Song

/**
 * Replaces "100x100bb" in artwork URL with a higher resolution.
 * The iTunes API returns 100px thumbnails, but supports arbitrary sizes.
 */
fun String.toHighResArtwork(size: Int = 600): String =
    replace("100x100bb", "${size}x${size}bb")

fun TrackDto.toSongEntity(searchQuery: String? = null): SongEntity? {
    // Filter out non-track results (the lookup API returns a "collection" wrapper as first result)
    if (wrapperType != "track" || trackId == null || trackName == null) return null

    return SongEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName.orEmpty(),
        collectionId = collectionId ?: 0L,
        collectionName = collectionName.orEmpty(),
        artworkUrl = artworkUrl100?.toHighResArtwork() ?: "",
        previewUrl = previewUrl,
        trackTimeMillis = trackTimeMillis ?: 0L,
        trackNumber = trackNumber ?: 0,
        searchQuery = searchQuery,
    )
}

fun TrackDto.toAlbumEntity(): AlbumEntity? {
    val id = collectionId ?: return null
    return AlbumEntity(
        collectionId = id,
        collectionName = collectionName.orEmpty(),
        artistName = artistName.orEmpty(),
        artworkUrl = artworkUrl100?.toHighResArtwork() ?: "",
        trackCount = trackCount ?: 0,
    )
}

fun SongEntity.toSong(): Song = Song(
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

fun AlbumEntity.toAlbum(): Album = Album(
    collectionId = collectionId,
    collectionName = collectionName,
    artistName = artistName,
    artworkUrl = artworkUrl,
    trackCount = trackCount,
)
