package com.bruno.itunessearch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bruno.itunessearch.data.local.entity.AlbumEntity
import com.bruno.itunessearch.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class, AlbumEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun albumDao(): AlbumDao

    companion object {
        const val NAME = "itunes_search.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_collectionId ON songs (collectionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_searchQuery ON songs (searchQuery)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_songs_lastPlayedAt ON songs (lastPlayedAt)")
            }
        }
    }
}
