package pl.app.deaftitles.parser

import pl.app.deaftitles.model.Subtitles

/**
 * Interface to provide parsed srt file
 * @see SubtitleParser
 */
interface SrtResult {

    /**
     * Provide parsed subtitles to UI
     * @property subtitles parsed srt
     */
    fun result(subtitles: Subtitles)

    /**
     * Inform UI when parse procedure start
     */
    fun startParse()

    /**
     * Inform UI when parse procedure end
     */
    fun endParse(parseResult: SubtitleParser.ParseResult)
}