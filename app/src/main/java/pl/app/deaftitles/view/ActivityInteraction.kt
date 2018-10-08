package pl.app.deaftitles.view

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.view.View
import pl.app.deaftitles.model.Subtitles

interface ActivityInteraction<T : Activity> {

    fun activity(): T

    fun getSubtitles(): LiveData<ArrayList<Subtitles>>

    fun addNewSubtitles(progressBar: View)

    fun onSubtitleChooseDestroyed()

    fun onCacheSubtitlesSelected(subtitles: Subtitles)

    fun deleteSubtitles(name: String)
}