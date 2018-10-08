package pl.app.deaftitles.processor

interface SubtitleProvider {

    fun onSubtitle(subtitle: String)

    fun onTime(time: String)

    fun onPauseResume(pause: Boolean)
}