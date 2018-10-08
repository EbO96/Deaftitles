package pl.app.deaftitles.data.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import pl.app.deaftitles.model.SrtCache

@Database(entities = [SrtCache::class], version = 1, exportSchema = false)
abstract class SubtitlesCacheDatabase : RoomDatabase() {
    abstract fun srtCacheDao(): SubtitlesCacheDao
}