package pl.app.deaftitles.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_deaftitles.*
import kotlinx.android.synthetic.main.interface_layout.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import pl.app.deaftitles.R
import pl.app.deaftitles.model.Srt
import pl.app.deaftitles.model.Subtitles
import pl.app.deaftitles.processor.SubtitleProvider
import pl.app.deaftitles.reader.SubtitlesReader
import pl.app.deaftitles.viewmodel.DeaftitlesViewModel

class DeaftitlesActivity : AppCompatActivity(), SubtitleProvider, ActivityInteraction<DeaftitlesActivity> {

    private val viewModel: DeaftitlesViewModel by inject {
        parametersOf(this@DeaftitlesActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deaftitles)

        optionsButton?.setOnClickListener(viewModel)

        pauseButton?.setOnClickListener(viewModel)

        cameraView?.setOnClickListener(viewModel)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            SubtitlesReader.READ_SRT -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.also { uri ->
                        viewModel.parseNewSubtitles(uri)
                    }
                }
            }

            DeaftitlesViewModel.CHOOSE_MOMENT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getParcelableExtra<Srt>(MomentActivity.SRT)?.apply {
                        viewModel.jumpToMoment(this)
                    }
                }
            }
        }

    }

    override fun onSubtitle(subtitle: String) {
        subtitleTextView?.text = subtitle
    }

    override fun onTime(time: String) {
        timeTextView?.text = time
    }

    override fun onPauseResume(pause: Boolean) {
        pauseButton?.isSelected = pause
    }

    override fun activity(): DeaftitlesActivity = this

    override fun getSubtitles() =
            viewModel.getCacheSubtitles()

    override fun addNewSubtitles(progressBar: View) {
        progressBar.visibility = View.VISIBLE
        viewModel.writeNewSubtitles()
    }

    override fun onSubtitleChooseDestroyed() {
        viewModel.backFromSubtitleChooseFragment()
    }

    override fun onCacheSubtitlesSelected(subtitles: Subtitles) {
        supportFragmentManager?.popBackStack()
        viewModel.onNewSubtitles(subtitles)
    }

    override fun deleteSubtitles(name: String) {
        viewModel.deleteSubtitle(name)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseSubtitles()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeSubtitles()
    }

    override fun onStart() {
        super.onStart()
        viewModel.startMoviePreview()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopMoviePreview()
    }
}
