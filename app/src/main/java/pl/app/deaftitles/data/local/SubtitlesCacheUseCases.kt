package pl.app.deaftitles.data.local

import pl.app.deaftitles.model.LastSrt
import pl.app.deaftitles.model.SrtCache
import pl.app.deaftitles.model.Subtitles

interface SubtitlesCacheUseCases {

    suspend fun saveSubtitle(srtCache: SrtCache)

    suspend fun deleteSubtitle(name: String)

    suspend fun getSubtitles(): List<Subtitles>

    suspend fun insertLastMoment(srt: LastSrt)

    suspend fun getLastMoment(): List<LastSrt>
}