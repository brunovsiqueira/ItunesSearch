package com.bruno.itunessearch.data.remote

import com.bruno.itunessearch.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {

    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("media") media: String = "music",
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 200,
        @Query("country") country: String = "US",
    ): SearchResponseDto

    @GET("lookup")
    suspend fun lookupAlbumTracks(
        @Query("id") collectionId: Long,
        @Query("entity") entity: String = "song",
    ): SearchResponseDto

    companion object {
        const val BASE_URL = "https://itunes.apple.com/"
    }
}
