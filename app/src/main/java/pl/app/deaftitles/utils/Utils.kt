package pl.app.deaftitles.utils

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

fun Long.timeToString(): String {
    val h = this / 1000 / 60 / 60
    val m = Math.abs((h * 60) - (this / 1000 / 60))
    val s = (this / 1000) - ((h * 3600) + (m * 60))
    return "${h.let { hour -> if (hour == 0L) "00" else "$hour" }}:${m.addZero()}:${s.addZero()}"
}

private fun Long.addZero() = this.let { seconds -> if (seconds < 10) "0$seconds" else "$seconds" }

fun AppCompatActivity.setToolbar(toolbarResId: Int, title: String? = null, displayHome: Boolean = false): Toolbar? {
    return findViewById<Toolbar>(toolbarResId)?.let { toolbar ->
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(displayHome)
        }
        toolbar
    }
}

fun AppCompatActivity.addFragment(vararg fragments: Fragment,
                                  container: Int,
                                  replace: Boolean = false,
                                  stackTag: String? = null) {
    with(supportFragmentManager.beginTransaction()) {
        fragments.forEach { fragment ->
            if (!replace) {
                add(container, fragment)
                stackTag?.let { addToBackStack(it) }
            } else replace(container, fragment)
        }
        commit()
    }
}

fun Long.toDisplayDate() = dateFormat.format(Date(this))
