package pl.app.deaftitles.data.local

import com.google.gson.Gson
import pl.app.deaftitles.data.database.SubtitlesCacheDao
import pl.app.deaftitles.model.LastSrt
import pl.app.deaftitles.model.SrtCache
import pl.app.deaftitles.model.Subtitles

class SubtitlesLocalCacheRepository(private val database: SubtitlesCacheDao) : SubtitlesCacheUseCases {

    override suspend fun saveSubtitle(srtCache: SrtCache) {
        database.insertSubtitle(srtCache)
    }

    override suspend fun deleteSubtitle(name: String) {
        database.deleteSubtitle(name)
    }

    override suspend fun getSubtitles(): List<Subtitles> {

        val srtCache = database.getSubtitles()

        val gson = Gson()

        return srtCache.map { cache ->
            gson.fromJson<Subtitles>(cache.subtitles, Subtitles::class.java)
        }
    }

    override suspend fun insertLastMoment(srt: LastSrt) {
        database.insertLastMoment(srt)
    }

    override suspend fun getLastMoment(): List<LastSrt> {
        return database.getLastMoment()
    }
}