package pl.app.deaftitles

import android.app.Application
import org.koin.android.ext.android.startKoin
import pl.app.deaftitles.di.cacheModule

class DeaftitlesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, arrayListOf(cacheModule))
    }
}