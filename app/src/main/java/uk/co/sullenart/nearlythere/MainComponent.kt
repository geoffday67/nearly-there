package uk.co.sullenart.nearlythere

import dagger.Component
import javax.inject.Singleton

@Component(modules = [MainModule::class])
@Singleton
interface MainComponent {
    fun inject(baseActivity: BaseActivity)
}