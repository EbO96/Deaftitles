package pl.app.deaftitles.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "last_srt")
class LastSrt(@PrimaryKey var id: Int = 1) {

    var name: String = ""

    var pauseTime: Long = 0L

    var timeRemaining: Long = -1L

}