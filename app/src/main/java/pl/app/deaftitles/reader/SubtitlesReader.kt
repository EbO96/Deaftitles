package pl.app.deaftitles.reader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * Class used to read srt file
 */
class SubtitlesReader {

    companion object {
        const val READ_SRT = 0
    }

    private var lines: List<String>? = null

    private var subtitleName = "no name"

    fun getSubtitleName() = subtitleName

    var subtitlesFileSize = 0f

    fun readSubtitlesFromFile(activity: Activity, uri: Uri): List<String> {

        //Get file name from file path
        subtitleName = uri.path.split("/").lastOrNull() ?: "no name"

        activity.contentResolver.openInputStream(uri)?.use { inputStream ->
            //Get file size
            subtitlesFileSize = inputStream.available() / 1000f

            BufferedReader(InputStreamReader(inputStream, Charset.forName("windows-1250"))).use { reader ->
                lines = reader.readLines()
            }
        }

        return lines?.filter { line -> line.trim().let { it.isNotEmpty() && it.isNotBlank() && it != "\n" } }
                ?: listOf()
    }

    fun searchSubtitlesFile(activity: Activity) {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"

        }
        activity.startActivityForResult(intent, READ_SRT)
    }

}