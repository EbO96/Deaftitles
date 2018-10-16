package pl.app.deaftitles.di

import android.arch.persistence.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import pl.app.deaftitles.data.SubtitleCacheRepository
import pl.app.deaftitles.data.database.SubtitlesCacheDatabase
import pl.app.deaftitles.data.local.SubtitlesLocalCacheRepository
import pl.app.deaftitles.viewmodel.DeaftitlesViewModel

val cacheModule = module {

    //Room database instance
    single {
        Room.databaseBuilder(
                androidApplication().applicationContext,
                SubtitlesCacheDatabase::class.java,
                "srt_cache_database")
                .fallbackToDestructiveMigration()
                .build()
                .srtCacheDao()
    }

    single { SubtitlesLocalCacheRepository(get()) }

    single { SubtitleCacheRepository(get()) }

    viewModel { parameterList -> DeaftitlesViewModel(get(), parameterList[0]) }
}