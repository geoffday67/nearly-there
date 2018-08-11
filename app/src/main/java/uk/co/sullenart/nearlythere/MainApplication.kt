package uk.co.sullenart.nearlythere

import android.app.Application
import com.facebook.stetho.Stetho
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainApplication : Application() {
    lateinit var component: MainComponent

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        Stetho.initializeWithDefaults(this);

        component = DaggerMainComponent.builder()
                .mainModule(MainModule(this))
                .build()
    }
}