package pl.app.deaftitles.data

import pl.app.deaftitles.data.local.SubtitlesCacheUseCases
import pl.app.deaftitles.data.local.SubtitlesLocalCacheRepository
import pl.app.deaftitles.model.Subtitles
import pl.app.deaftitles.model.SrtCache

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

}