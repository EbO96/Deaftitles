package pl.app.deaftitles.parser

import android.os.AsyncTask
import pl.app.deaftitles.model.Srt
import pl.app.deaftitles.model.Subtitles
import pl.app.deaftitles.utils.toDisplayDate

/**
 * Class user to parseLines .srt file to array with every element contains
 * Srt object witch contains dateRange, line number and subtitle
 * @property lines not modified, clear srt file lines as list
 * @property srtResult callback to provide results to UI
 * @see Srt
 */
class SubtitleParser(private val lines: List<String>, private val srtResult: SrtResult, private val fileSize: Float = 0f) : AsyncTask<Unit, Unit, ArrayList<Srt>?>() {

    enum class ParseResult {
        OK,
        ERROR
    }

    /**
     * Parse array list which contains lines loaded from srt file
     * @see Srt
     * @throws SubtitleParseException when used try to parse other file than *.srt file
     * @return list of srt object.
     */
    @Throws(SubtitleParseException::class)
    private fun parseLines(): ArrayList<Srt> {

        //Output array
        val listOfSrt = ArrayList<Srt>() //Parsed srt file

        //Find lines with timecodes
        val textIndexes = lines.asSequence().withIndex().map { t ->
            if (t.value.contains("-->")) t.index else -1
        }.filter { index -> index != -1 }.toList().plus(lines.size + 1)

        //Subtitles from whole file
        val subtitles = ArrayList<String>()

        //Get text from between two timecodes
        textIndexes.zipWithNext { lineIndex, nextLineIndex ->
            val lineBuilder = StringBuilder()
            for (x in lineIndex + 1 until nextLineIndex - 1) {
                val line = lines[x]
                lineBuilder.append(line)
                if (x < nextLineIndex - 2)
                    lineBuilder.append("\n")
            }

            subtitles.add("$lineBuilder")
        }

        val textsSize = subtitles.size

        lines.withIndex().forEach {
            if (it.value.contains("-->")) {

                val srt = Srt()
                val index = it.index

                //Get line number. In srt file, line number always appears above timecode
                val lineNumber = lines[index - 1].toInt()
                srt.line = lineNumber

                val lineIndex = lineNumber - 1
                //Add subtitle
                if (lineIndex < textsSize) {
                    srt.subtitle = subtitles[lineIndex]
                }

                //Parse string timecodes to long timecodes
                it.value.split("-->").withIndex().forEach {
                    val time = it.value.trim().replace(",", ":").split(":")
                    val formattedTime = time.map { t -> if (t == "00") 0L else t.toLong() }

                    val h = formattedTime[0] * 60 * 60 * 1000 //hours to ms
                    val m = formattedTime[1] * 60 * 1000 // minutes to ms
                    val s = formattedTime[2] * 1000 //seconds to ms
                    val ms = formattedTime[3] //ms

                    val t = h + m + s + ms

                    if (it.index == 0) {
                        srt.startTime = t
                        srt.startTimeString = it.value.trim()
                    } else {
                        srt.endTime = t
                        srt.endTimeString = it.value.trim()
                    }
                }

                listOfSrt.add(srt)
            }
        }
        return listOfSrt
    }

    init {
        execute()
    }

    override fun doInBackground(vararg p0: Unit?): ArrayList<Srt>? {
        srtResult.startParse()
        return try {
            parseLines()
        } catch (e: Exception) {
            null
        }
    }

    override fun onPostExecute(result: ArrayList<Srt>?) {
        //Return parsed subtitles
        if (result == null) {
            srtResult.endParse(ParseResult.ERROR)
            return
        }

        srtResult.result(Subtitles(result).apply {
            timestamp = System.currentTimeMillis()
            timestampString = timestamp.toDisplayDate()
            movieLength = result.lastOrNull()?.endTime ?: 0L
            fileSize = this@SubtitleParser.fileSize
        })
        srtResult.endParse(ParseResult.OK)
    }
}