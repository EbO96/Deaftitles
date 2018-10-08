package pl.app.deaftitles.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_moment.*
import kotlinx.android.synthetic.main.moment_item.view.*
import pl.app.deaftitles.R
import pl.app.deaftitles.adapter.adapter
import pl.app.deaftitles.model.Srt
import pl.app.deaftitles.utils.setToolbar

class MomentActivity : AppCompatActivity() {

    companion object {
        const val SRT = "SRT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moment)

        setToolbar(R.id.myToolbar, getString(R.string.moment), true)

        val items = intent?.getParcelableArrayListExtra<Srt>(SRT)

        if (items != null)
            recyclerView?.adapter(
                    item = R.layout.moment_item,
                    items = items,
                    decoration = true,
                    viewHolder = { srt, view ->
                        view.apply {
                            lineTextView.text = "${srt.line}"
                            startTimeTextView.text = srt.startTimeString
                            endTimeTextView.text = srt.endTimeString
                            subtitleTextView.text = srt.subtitle
                        }
                    },
                    click = { srt ->
                        val chooseSrt = Intent().apply { putExtra(SRT, srt) }
                        setResult(Activity.RESULT_OK, chooseSrt)
                        finish()
                    })
    }
}
