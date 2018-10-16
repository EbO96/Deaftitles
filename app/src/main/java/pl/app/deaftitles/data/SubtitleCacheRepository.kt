package pl.app.deaftitles.data

import pl.app.deaftitles.data.local.SubtitlesCacheUseCases
import pl.app.deaftitles.data.local.SubtitlesLocalCacheRepository
import pl.app.deaftitles.model.LastSrt
import pl.app.deaftitles.model.SrtCache
import pl.app.deaftitles.model.Subtitles

class SubtitleCacheRepository(private val subtitlesLocalCacheRepository: SubtitlesLocalCacheRepository) :
        SubtitlesCacheUseCases {

    override suspend fun saveSubtitle(srtCache: SrtCache) {
        subtitlesLocalCacheRepository.saveSubtitle(srtCache)
    }

    override suspend fun deleteSubtitle(name: String) {
        subtitlesLocalCacheRepository.deleteSubtitle(name)
    }

    override suspend fun getSubtitles(): List<Subtitles> {
        return subtitlesLocalCacheRepository.getSubtitles()
    }

    override suspend fun insertLastMoment(srt: LastSrt) {
        return subtitlesLocalCacheRepository.insertLastMoment(srt)
    }

    override suspend fun getLastMoment(): List<LastSrt> {
        return subtitlesLocalCacheRepository.getLastMoment()
    }
}