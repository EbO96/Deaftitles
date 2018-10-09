package pl.app.deaftitles.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.support.annotation.UiThread
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import com.google.gson.Gson
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import kotlinx.android.synthetic.main.activity_deaftitles.*
import kotlinx.android.synthetic.main.interface_layout.*
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import pl.app.deaftitles.R
import pl.app.deaftitles.data.SubtitleCacheRepository
import pl.app.deaftitles.fragment.SubtitlesChooseFragment
import pl.app.deaftitles.model.Srt
import pl.app.deaftitles.model.SrtCache
import pl.app.deaftitles.model.Subtitles
import pl.app.deaftitles.parser.SrtResult
import pl.app.deaftitles.parser.SubtitleParser
import pl.app.deaftitles.processor.SubtitlesProcessor
import pl.app.deaftitles.reader.SubtitlesReader
import pl.app.deaftitles.utils.addFragment
import pl.app.deaftitles.view.ActivityInteraction
import pl.app.deaftitles.view.DeaftitlesActivity
import pl.app.deaftitles.view.MomentActivity

class DeaftitlesViewModel(private val subtitlesCacheRepository: SubtitleCacheRepository,
                          private val activityInteraction: ActivityInteraction<DeaftitlesActivity>) :
        ViewModel(),
        View.OnClickListener,
        PopupMenu.OnMenuItemClickListener,
        View.OnSystemUiVisibilityChangeListener,
        SrtResult {

    companion object {
        const val CHOOSE_MOMENT_CODE = 1
        const val UI_VISIBILITY_TIME = 3500L
    }

    private val parseErrorMessage = activityInteraction.activity().getString(R.string.parse_error_message)

    private var hideInterfaceTimer: CountDownTimer? = null

    private val subtitlesReader by lazy { SubtitlesReader() }
    private var subtitleProcessor: SubtitlesProcessor? = null

    //Camera
    private var camera: Fotoapparat = newCameraInstance()

    fun newCameraInstance() = Fotoapparat(
            context = activityInteraction.activity(),
            view = activityInteraction.activity().cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back()
    )

    //Options menu
    private val popupMenu: PopupMenu = PopupMenu(activityInteraction.activity(), activityInteraction.activity().optionsButton).apply {
        menuInflater.inflate(R.menu.main_menu, menu)
        setOnMenuItemClickListener(this@DeaftitlesViewModel)
    }

    private var isUiVisible = true

    private var isUiAutoHide = true

    //Automatic ui hide control flag
    private var isUiAutoHideEnabled = false

    private val newSubtitles = MutableLiveData<ArrayList<Subtitles>>()

    init {
        activityInteraction.activity().window.decorView.setOnSystemUiVisibilityChangeListener(this)
    }

    @UiThread
    fun onNewSubtitles(subtitles: Subtitles) {

        activityInteraction.activity().apply {

            subtitleProcessor?.cancel()

            launch {
                cacheSubtitle(subtitles)
                getCacheSubtitles()
            }

            subtitleProcessor = SubtitlesProcessor(this, subtitles, this)
        }
    }

    fun writeNewSubtitles() {
        subtitlesReader.searchSubtitlesFile(activity = activityInteraction.activity())
    }

    fun pauseSubtitles() {
        subtitleProcessor?.pause()
    }

    fun resumeSubtitles() {
        subtitleProcessor?.resume()
    }

    fun startMoviePreview() {
        pauseSubtitles()
        camera.start()
    }

    fun stopMoviePreview() {
        pauseSubtitles()
        camera.stop()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.pauseButton -> {
                subtitleProcessor?.pauseResume()
            }
            R.id.cameraView -> {
                if (isUiVisible) hideSystemUI()
                else showSystemUI()
            }
            R.id.optionsButton -> {
                popupMenu.show()
            }
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.subtitles -> {
                activityInteraction.activity().apply {

                    isUiAutoHide = false

                    hideInterfaceTimer?.cancel()

                    pauseSubtitles()

                    showSystemUI()

                    addFragment(
                            fragments = *arrayOf(SubtitlesChooseFragment()),
                            container = container.id,
                            stackTag = SubtitlesChooseFragment::class.java.name)
                }
            }
            R.id.moment -> {
                activityInteraction.activity().apply {
                    val intent = Intent(this, MomentActivity::class.java).apply {
                        putParcelableArrayListExtra(MomentActivity.SRT, subtitleProcessor?.subtitles?.srt)
                    }
                    startActivityForResult(intent, CHOOSE_MOMENT_CODE)
                }
            }
        }
        return false
    }

    fun jumpToMoment(srt: Srt) {
        subtitleProcessor?.jumTo(srt)
    }

    @UiThread
    suspend fun cacheSubtitle(subtitles: Subtitles) {
        val cache = SrtCache().apply {
            name = subtitles.name
            this.subtitles = Gson().toJson(subtitles)
        }
        subtitlesCacheRepository.saveSubtitle(cache)
    }

    fun getCacheSubtitles(): LiveData<ArrayList<Subtitles>> {
        launch(UI) {
            val subtitles = withContext(DefaultDispatcher) { subtitlesCacheRepository.getSubtitles() as ArrayList<Subtitles> }
            subtitles.sortBy { s -> s.timestamp }
            subtitles.reverse()
            newSubtitles.value = subtitles
        }
        return newSubtitles
    }

    /*
    Srt parse callbacks
     */

    private fun findSubtitlesChooseFragment(block: (SubtitlesChooseFragment) -> Unit) {
        val fragment = activityInteraction.activity()
                .supportFragmentManager
                .fragments
                .find {
                    it is SubtitlesChooseFragment
                } as? SubtitlesChooseFragment
        if (fragment != null) block(fragment)
    }

    override fun startParse() {
        findSubtitlesChooseFragment {
            it.showLoading()
        }
    }

    override fun endParse(parseResult: SubtitleParser.ParseResult) {
        when (parseResult) {
            SubtitleParser.ParseResult.OK -> {

            }
            SubtitleParser.ParseResult.ERROR -> {
                Toast.makeText(activityInteraction.activity(), parseErrorMessage, Toast.LENGTH_LONG).show()
            }
        }
        findSubtitlesChooseFragment {
            it.hideLoading()
        }
    }

    /**
     * parse new loaded srt
     */
    fun parseNewSubtitles(uri: Uri) {
        val lines = subtitlesReader.readSubtitlesFromFile(activityInteraction.activity(), uri)
        if (lines.isNotEmpty()) {
            SubtitleParser(lines, this, fileSize = subtitlesReader.subtitlesFileSize)
        } else endParse(SubtitleParser.ParseResult.ERROR)
    }

    //Subtitle parser result
    override fun result(subtitles: Subtitles) {
        //Set subtitle name
        subtitles.name = subtitlesReader.getSubtitleName()
        onNewSubtitles(subtitles)
    }

    /**
     * Delete subtitles from local cache
     */
    fun deleteSubtitle(name: String) {
        launch {
            withContext(DefaultDispatcher) { subtitlesCacheRepository.deleteSubtitle(name) }
            getCacheSubtitles()
        }
    }

    /*
    System UI and interface
     */

    /**
     * Hide my player interface
     */
    private fun hideInterface() {
        hideInterfaceTimer = object : CountDownTimer(UI_VISIBILITY_TIME, UI_VISIBILITY_TIME) {
            override fun onFinish() {
                popupMenu.dismiss()
                hideSystemUI()
            }

            override fun onTick(p0: Long) {
            }

        }.start()
    }

    /**
     * Hide android system UI
     */
    private fun hideSystemUI() {
        activityInteraction.activity().apply {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    /**
     * Show android system UI
     */
    private fun showSystemUI() {
        activityInteraction.activity().apply {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    override fun onSystemUiVisibilityChange(visibility: Int) {

        /*
        React at android system UI changes
         */
        activityInteraction.activity().interfaceView?.visibility = if (visibility == 0) {
            hideInterfaceTimer?.cancel()
            if (isUiAutoHide && isUiAutoHideEnabled)
                hideInterface()
            isUiVisible = true
            View.VISIBLE
        } else {
            isUiVisible = false
            View.INVISIBLE
        }

    }

    /**
     * Callback function triggered when subtitle choose screen is destroyed
     * @see SubtitlesChooseFragment
     */
    fun backFromSubtitleChooseFragment() {
        isUiAutoHide = true
        hideSystemUI()
    }

}