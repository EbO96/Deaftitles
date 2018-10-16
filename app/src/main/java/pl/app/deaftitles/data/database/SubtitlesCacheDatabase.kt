package pl.app.deaftitles.data.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import pl.app.deaftitles.model.LastSrt
import pl.app.deaftitles.model.SrtCache

@Database(entities = [SrtCache::class, LastSrt::class], version = 1, exportSchema = false)
abstract class SubtitlesCacheDatabase : RoomDatabase() {
    abstract fun srtCacheDao(): SubtitlesCacheDao
}