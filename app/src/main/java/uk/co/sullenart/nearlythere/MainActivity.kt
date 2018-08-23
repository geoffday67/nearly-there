/*
Create an ongoing notification, use it set the DestinationService to foreground.
*/

package uk.co.sullenart.nearlythere

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.PopupMenu
import butterknife.BindView
import butterknife.OnClick
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import uk.co.sullenart.nearlythere.destination.AddDestinationActivity
import uk.co.sullenart.nearlythere.model.Subject

const val MONITORING_CHANNEL_NAME = "Destination monitoring"
const val MONITORING_CHANNEL_ID = "monitoring"
const val MONITORING_NOTIFICATION_ID = 1

class MainActivity : BaseActivity(R.layout.activity_main) {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.add_destination)
    lateinit var addDestination: FloatingActionButton

    @BindView(R.id.destination_list)
    lateinit var destinationList: ListView

    lateinit var subjectAdapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .take(1)
                .subscribe {
                    // Set up database with some sample data for testing
                    /*with(destinationDao) {
                        clear()
                        addDestination(Destination("BoA", 51.34491248869605, -2.252326638171736, true))
                        addDestination(Destination("Somewhere else", 51.0, -2.0, false))
                        //addDestination(Destination("Home", 51.4273413, -2.2255878, true))
                        addDestination(Destination("Temple Meads", 51.4497534, -2.583208, true))
                    }*/

                    // Show the list of destinations
                    subjectAdapter = SubjectAdapter(this, subjectMenuListener)
                    destinationList.adapter = subjectAdapter

                    // Start a foreground service so we keep running
                    DestinationService.start(this)

                    monitorDestinations()

                    addDestination.setOnClickListener {
                        AddDestinationActivity.start(this)
                    }

                    Timber.d("Activity created")
                }
    }

    override fun onDestroy() {
        super.onDestroy()

        destinationManager.cancelAll()
        DestinationService.stop(this)

        Timber.d("Activity destroyed")
    }

    override fun onNewIntent(intent: Intent) {
        when (intent.action) {
            AlertManager.ACTION_DELETE_ALERT -> subjectAdapter.setHighlightByName(intent.getStringExtra(AlertManager.EXTRA_DELETE_ALERT_NAME), false)
            else -> destinationManager.handleNewIntent(intent)
        }
    }

    private val subjectMenuListener = object : SubjectMenuListener {
        override fun onMenu(subject: Subject, view: View) {
            val popup = PopupMenu(this@MainActivity, view).apply {
                inflate(R.menu.destination_popup_menu)
            }
            val menu = popup.menu
            menu.findItem(R.id.destination_enable).setVisible(!subject.destination.active)
            menu.findItem(R.id.destination_disable).setVisible(subject.destination.active)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.destination_disable -> dataManager.setDestinationActive(subject.destination, false)
                    R.id.destination_enable -> dataManager.setDestinationActive(subject.destination, true)
                    R.id.destination_delete -> dataManager.deleteDestination(subject.destination)
                }

                true
            }

            popup.show()
        }
    }

    private fun monitorDestinations() {
        compositeDisposable.add(dataManager.getAllDestinations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { destinations ->
                    Timber.d("Monitoring ${destinations.size} possible destination(s) from database")

                    destinationManager.setDestinations(Intent(this, MainActivity::class.java),
                            destinations.filter { it.active })

                    subjectAdapter.clear()
                    destinations.forEach { subjectAdapter.add(Subject(it)) }
                    subjectAdapter.notifyDataSetChanged()
                }
        )

        // React to entering and leaving destination areas
        // TODO Start an ongoing notification if near a destination (AlarmManager?)
        compositeDisposable.add(destinationManager.enteredDestinations
                .map { Subject(destination = it, highlighted = true) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Timber.d("Entered ${it.destination.name}")
                    subjectAdapter.update(it)
                    alertManager.alertDestination(it.destination)
                }
        )

    }

    @OnClick(R.id.quit)
    fun onQuit() {
        finish()
    }

    private fun onAddClick() {
        AddDestinationActivity.start(this)
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
