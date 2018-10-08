package pl.app.deaftitles.model

import android.arch.persistence.room.Ignore
import com.google.gson.annotations.SerializedName

data class Subtitles(@Ignore @SerializedName("subtitles") val srt: ArrayList<Srt>,
                     @SerializedName("name") var name: String = "no name",
                     @SerializedName("timestampString") var timestampString: String = "",
                     @SerializedName("timestamp") var timestamp: Long = 0L,
                     @SerializedName("movieLength") var movieLength: Long = 0L,
                     @SerializedName("fileSize") var fileSize: Float = 0f)