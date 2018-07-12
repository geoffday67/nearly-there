/*
Set up geofences for areas of interest.
Subscribe to entered areas, notify entered area with sound, etc.
Foreground service for monitoring/notifying.
Subscribe to list of nearby destinations, update UI to match.
 */
package uk.co.sullenart.nearlythere

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import butterknife.BindView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import uk.co.sullenart.nearlythere.model.Destination
import uk.co.sullenart.nearlythere.model.Subject

class MainActivity : BaseActivity(R.layout.activity_main) {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.add_destination)
    lateinit var addDestination: FloatingActionButton

    @BindView(R.id.destination_list)
    lateinit var destinationList: ListView

    lateinit var subjectAdapter: SubjectAdapter
    lateinit var destinationService: DestinationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        with(destinationDao) {
            clear()
            addDestination(Destination("BoA", 51.34491248869605, -2.252326638171736, true))
            addDestination(Destination("Somewhere else", 51.0, -2.0, false))
        }

        subjectAdapter = SubjectAdapter(this)
        destinationList.adapter = subjectAdapter

        DestinationService.start(this@MainActivity)
                .subscribeBy {
                    Timber.d("Destination service created")
                    destinationService = it.destinationService

                    destinationDao.getAllDestinations()
                            .take(1)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { destinations ->
                                Timber.d("${destinations.size} destination(s) from database")
                                destinationService.setDestinations(destinations.filter { it.active })
                                subjectAdapter.clear()
                                destinations.forEach { subjectAdapter.add(Subject(it)) }
                            }

                    Flowable.merge(
                            destinationService.enteredDestinations
                                    .map { Subject(it, true) },
                            destinationService.leftDestinations
                                    .map { Subject(it, false) }
                    )
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                subjectAdapter.update(it)
                            }
                }

        addDestination.setOnClickListener { onAddClick() }

/*val start = System.currentTimeMillis()
assets.open("stations.dat").use {
stationManager.loadStations(it)
}
val end = System.currentTimeMillis()
Timber.d("%d stations loaded in %d milliseconds", stationManager.stationCount, end - start);*/
    }

    private fun onAddClick() {
        AddDestinationActivity.launch(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
