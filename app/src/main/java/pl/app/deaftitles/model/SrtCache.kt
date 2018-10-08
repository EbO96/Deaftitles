package pl.app.deaftitles.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "cached_subtitles")
class SrtCache {

    @PrimaryKey
    @ColumnInfo(name = "name")
    var name = ""

    @ColumnInfo(name = "subtitles")
    var subtitles = ""
}