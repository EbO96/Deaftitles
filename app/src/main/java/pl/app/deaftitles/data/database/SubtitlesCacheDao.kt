package pl.app.deaftitles.data.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import pl.app.deaftitles.model.LastSrt
import pl.app.deaftitles.model.SrtCache

@Dao
interface SubtitlesCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubtitle(srtCache: SrtCache)

    @Query("DELETE FROM cached_subtitles WHERE name LIKE :name")
    fun deleteSubtitle(name: String)

    @Query("SELECT * FROM cached_subtitles")
    fun getSubtitles(): List<SrtCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLastMoment(srt: LastSrt)

    @Query("SELECT * FROM last_srt")
    fun getLastMoment(): List<LastSrt>
}