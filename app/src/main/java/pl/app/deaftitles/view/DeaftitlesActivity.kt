package pl.app.deaftitles.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_deaftitles.*
import kotlinx.android.synthetic.main.interface_layout.*
import pl.app.deaftitles.R
import pl.app.deaftitles.model.Srt
import pl.app.deaftitles.model.Subtitles
import pl.app.deaftitles.parser.SubtitleParser
import pl.app.deaftitles.processor.SubtitleProvider
import pl.app.deaftitles.reader.SubtitlesReader
import pl.app.deaftitles.viewmodel.DeaftitlesViewModel

class DeaftitlesActivity : MyActivity(), SubtitleProvider, ActivityInteraction<DeaftitlesActivity> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deaftitles)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
                } else viewModel.endParse(SubtitleParser.ParseResult.OK)
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

}
