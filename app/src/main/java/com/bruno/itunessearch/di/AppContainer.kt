package com.bruno.itunessearch.di

import android.content.Context
import androidx.room.Room
import com.bruno.itunessearch.data.AndroidConnectivityObserver
import com.bruno.itunessearch.data.ConnectivityObserver
import com.bruno.itunessearch.data.local.AppDatabase
import com.bruno.itunessearch.data.remote.ITunesApi
import com.bruno.itunessearch.data.repository.AlbumRepositoryImpl
import com.bruno.itunessearch.data.repository.SongRepositoryImpl
import com.bruno.itunessearch.domain.repository.AlbumRepository
import com.bruno.itunessearch.domain.repository.SongRepository
import com.bruno.itunessearch.player.AudioPlayer
import com.bruno.itunessearch.player.ExoAudioPlayer
import com.bruno.itunessearch.player.NowPlayingState
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class AppContainer(context: Context) {

    val applicationContext: Context = context.applicationContext

    // Connectivity
    val connectivityObserver: ConnectivityObserver = AndroidConnectivityObserver(applicationContext)

    // Audio — singletons, shared across all screens
    val audioPlayer: AudioPlayer = ExoAudioPlayer(applicationContext)
    val nowPlaying: NowPlayingState = NowPlayingState()

    // Network
    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ITunesApi.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: ITunesApi = retrofit.create(ITunesApi::class.java)

    // Database
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.NAME,
    )
        .addMigrations(AppDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration() // safe — only cached data, no user-created data
        .build()

    // Repositories
    val songRepository: SongRepository = SongRepositoryImpl(
        api = api,
        songDao = database.songDao(),
    )

    val albumRepository: AlbumRepository = AlbumRepositoryImpl(
        api = api,
        albumDao = database.albumDao(),
        songDao = database.songDao(),
    )
}
