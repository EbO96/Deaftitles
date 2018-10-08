package pl.app.deaftitles.processor

import android.app.Activity
import android.os.CountDownTimer
import pl.app.deaftitles.model.Srt
import pl.app.deaftitles.model.Subtitles
import pl.app.deaftitles.utils.timeToString

class SubtitlesProcessor(private val activity: Activity, val subtitles: Subtitles,
                         private val subtitleProvider: SubtitleProvider) {

    //Set movie length
    private var movieLength = subtitles.srt.lastOrNull()?.endTime
            ?: throw NoSubtitleException()

    private var movieLengthString = movieLength.timeToString()

    private var pauseTime = movieLength

    private var timeRemaining = -1L

    private var countDownTimer: CountDownTimer? = null

    private var isPaused = false

    private var lastDisplayedSubtitle = ""

    init {
        start()
    }

    fun start() {

        isPaused = false

        subtitleProvider.onPauseResume(isPaused)

        countDownTimer = object : CountDownTimer(pauseTime, 200) {

            override fun onFinish() {

            }

            override fun onTick(t: Long) {

                val time = movieLength - t

                timeRemaining = t

                activity.runOnUiThread {
                    subtitleProvider.onTime("${t.timeToString()} / $movieLengthString")
                }

                subtitles.srt
                        .asSequence()
                        .filter { srt -> time in srt.startTime..srt.endTime }
                        .firstOrNull()
                        ?.apply {
                            if (this.subtitle != lastDisplayedSubtitle) {
                                activity.runOnUiThread {
                                    subtitleProvider.onSubtitle(this.subtitle)
                                }
                                lastDisplayedSubtitle = this.subtitle
                            }
                        }
                        ?: kotlin.run {
                            if (lastDisplayedSubtitle != "") {
                                lastDisplayedSubtitle = ""
                                activity.runOnUiThread {
                                    subtitleProvider.onSubtitle("")
                                }
                            }
                        }
            }
        }
        countDownTimer?.start()
    }

    fun pauseResume() {
        if (isPaused) resume()
        else pause()
    }

    fun pause() {
        if (!isPaused) {
            isPaused = true
            pauseTime = timeRemaining
            countDownTimer?.cancel()
        }
        subtitleProvider.onPauseResume(isPaused)
    }

    fun jumTo(srt: Srt) {
        timeRemaining = movieLength - srt.startTime
        isPaused = false
        pause()
    }

    fun resume() {
        isPaused = false
        start()
    }

    fun cancel() {
        subtitleProvider.apply {
            onSubtitle("")
            onTime("")
        }
        countDownTimer?.cancel()
    }
}