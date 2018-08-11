package uk.co.sullenart.nearlythere

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import butterknife.ButterKnife
import io.reactivex.disposables.CompositeDisposable
import uk.co.sullenart.nearlythere.data.DestinationDao
import javax.inject.Inject

open class BaseActivity(@LayoutRes private val content: Int) : AppCompatActivity() {
    @Inject
    protected lateinit var destinationDao: DestinationDao

    @Inject
    protected lateinit var stationManager: StationManager

    @Inject
    protected lateinit var destinationManager: DestinationManager

    @Inject
    protected lateinit var alertManager: AlertManager

    protected val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(content)
        ButterKnife.bind(this)
        (application as MainApplication).component.inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.dispose()
    }
}