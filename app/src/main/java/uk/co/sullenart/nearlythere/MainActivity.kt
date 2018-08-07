/*
Create an ongoing notification, use it set the DestinationService to foreground.
*/

package uk.co.sullenart.nearlythere

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import butterknife.BindView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import uk.co.sullenart.nearlythere.model.Destination
import uk.co.sullenart.nearlythere.model.Subject

private const val MONITORING_CHANNEL_NAME = "Destination monitoring"
private const val MONITORING_CHANNEL_ID = "monitoring"

private const val ALERT_CHANNEL_NAME = "Destination alert"
private const val ALERT_CHANNEL_ID = "alert"

class MainActivity : BaseActivity(R.layout.activity_main) {

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.add_destination)
    lateinit var addDestination: FloatingActionButton

    @BindView(R.id.destination_list)
    lateinit var destinationList: ListView

    lateinit var subjectAdapter: SubjectAdapter
    //val destinationService = DestinationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)

        // TODO Get permissions

        // Create notification channel for Android >= 26
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(
                    NotificationChannel(MONITORING_CHANNEL_ID, MONITORING_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            )

            notificationManager.createNotificationChannel(
                    NotificationChannel(ALERT_CHANNEL_ID, ALERT_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)//HIGH)
            )
        }

        with(destinationDao) {
            clear()
            addDestination(Destination("BoA", 51.34491248869605, -2.252326638171736, true))
            addDestination(Destination("Somewhere else", 51.0, -2.0, false))
            addDestination(Destination("Home", 51.4273413, -2.2255878, true))
            addDestination(Destination("Temple Meads", 51.4497534,-2.583208, true))
        }

        /*
        Start the service, then bind to it separately, so it isn't stopped when this activity is destroyed and unbinds.
         */

        // Start up the destination monitoring service
        Intent(this, DestinationService::class.java).let {
            bindService(it, destinationConnection, Context.BIND_AUTO_CREATE)
        }

        subjectAdapter = SubjectAdapter(this)
        destinationList.adapter = subjectAdapter

        addDestination.setOnClickListener { onAddClick() }

/*val start = System.currentTimeMillis()
assets.open("stations.dat").use {
stationManager.loadStations(it)
}
val end = System.currentTimeMillis()
Timber.d("%d stations loaded in %d milliseconds", stationManager.stationCount, end - start);*/
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(destinationConnection)
    }

    // Object which handles connection to destination service
    private val destinationConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Timber.e("Error connecting to destination service")
        }

        override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
            Timber.d("Connected to destination service")

            val destinationService = (binder as DestinationBinder).destinationService
            destinationService.setForeground(getMonitoringNotification())
            monitorDestinations(destinationService)
        }
    }

    private fun monitorDestinations(destinationService: DestinationService) {
        // Get the current destinations in the database and start monitoring them
        // TODO React to changes in the database, e.g. remove take(1)?
        destinationDao.getAllDestinations()
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { destinations ->
                    Timber.d("Monitoring ${destinations.size} possible destination(s) from database")
                    destinationService.setDestinations(destinations.filter { it.active })
                    subjectAdapter.clear()
                    destinations.forEach { subjectAdapter.add(Subject(it)) }
                }

        // React to entering and leaving destination areas
        // TODO Start an ongoing notification if near a destination (AlarmManager?)
        Flowable.merge(
                destinationService.enteredDestinations
                        .map { Subject(it, true) },
                destinationService.leftDestinations
                        .map { Subject(it, false) }
        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    subjectAdapter.update(it)

                    if (it.nearby) {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(2, getAlertNotification(it.destination))
                    }
                }
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

    fun getMonitoringNotification() =
            NotificationCompat.Builder(this, MONITORING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_location)
                    .setContentTitle("Nearly There")
                    .setContentText("Checking if you're nearly there yet")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setChannelId(MONITORING_CHANNEL_ID)
                    .build()

    fun getAlertNotification(destination: Destination) =
            NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_location)
                    .setContentTitle("Nearly at ${destination.name}")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setChannelId(ALERT_CHANNEL_ID)
                    .build()
}
