package uk.co.sullenart.nearlythere

import android.app.Application
import com.facebook.stetho.Stetho

class MainApplication : Application() {
    lateinit var component: DataComponent

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this);

        component = DaggerDataComponent.builder()
                .dataModule(DataModule(this))
                .build()

        //component.inject(this)
    }
}