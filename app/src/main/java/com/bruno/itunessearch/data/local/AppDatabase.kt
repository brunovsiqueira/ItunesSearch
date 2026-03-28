package com.bruno.itunessearch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bruno.itunessearch.data.local.entity.AlbumEntity
import com.bruno.itunessearch.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class, AlbumEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao

    companion object {
        const val NAME = "itunes_search.db"
    }
}
