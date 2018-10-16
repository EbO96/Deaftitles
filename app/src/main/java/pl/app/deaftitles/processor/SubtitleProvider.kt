package pl.app.deaftitles.processor

import pl.app.deaftitles.model.Srt

interface SubtitleProvider {

    fun onSubtitle(srt: Srt?)

    fun onTime(time: String)

    fun onPauseResume(pause: Boolean)
}