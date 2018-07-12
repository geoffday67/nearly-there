package uk.co.sullenart.nearlythere

import android.app.Application
import com.facebook.stetho.Stetho
import timber.log.Timber
import timber.log.Timber.DebugTree

class MainApplication : Application() {
    lateinit var component: DataComponent

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }

        Stetho.initializeWithDefaults(this);

        component = DaggerDataComponent.builder()
                .dataModule(DataModule(this))
                .build()
    }
}