package uk.co.sullenart.nearlythere

import dagger.Component
import javax.inject.Singleton

@Component(modules = [DataModule::class])
@Singleton
interface DataComponent {
    fun inject(baseActivity: BaseActivity)
}