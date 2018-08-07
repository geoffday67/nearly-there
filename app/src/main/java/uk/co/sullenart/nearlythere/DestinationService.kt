package uk.co.sullenart.nearlythere

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.*
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import uk.co.sullenart.nearlythere.model.Destination

class DestinationService : Service() {
    companion object {
        private const val DEFAULT_RADIUS = 2000.0f
        private const val REQUEST_GEOFENCE = 1
        private const val ACTION_GEOFENCE = "geofence"
        private const val NOTIFICATION_CHANNEL_ID = "nearly-there-id"
        private const val NOTIFICATION_ID = 1
    }

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geoPendingIntent: PendingIntent

    private val monitoredDestinations = HashMap<String, Destination>()

    private val enteredProcessor: PublishProcessor<Destination> = PublishProcessor.create()
    val enteredDestinations: Flowable<Destination>
        get() = enteredProcessor.share()

    private val leftProcessor: PublishProcessor<Destination> = PublishProcessor.create()
    val leftDestinations: Flowable<Destination>
        get() = leftProcessor.share()

    override fun onBind(intent: Intent?): IBinder = DestinationBinder(this)

    override fun onCreate() {
        geofencingClient = LocationServices.getGeofencingClient(this)

        val intent = Intent(this, DestinationService::class.java).apply {
            action = ACTION_GEOFENCE
        }
        geoPendingIntent = PendingIntent.getService(this, REQUEST_GEOFENCE, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        Timber.d("Destination service created")
    }

    fun setForeground(notification: Notification) {
        startForeground(NOTIFICATION_ID, notification)

        Timber.d("Destination service set to foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_GEOFENCE -> {
                val event = GeofencingEvent.fromIntent(intent)
                when (event.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER ->
                        event.triggeringGeofences.forEach {
                            Timber.d("Geofence entry detected ${it.requestId}")
                            monitoredDestinations[it.requestId]?.let {
                                enteredProcessor.onNext(it)
                            }
                        }

                    Geofence.GEOFENCE_TRANSITION_EXIT ->
                        event.triggeringGeofences.forEach {
                            Timber.d("Geofence exit detected ${it.requestId}")
                            monitoredDestinations[it.requestId]?.let {
                                leftProcessor.onNext(it)
                            }
                        }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Timber.d("Destination service destroyed")
    }

    private fun notifyNearlyThere(where: String) {
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle("Nearly There")
                .setContentText("Almost at $where")
                .setLights(0xFFA500, 250, 250)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(0, 500, 500, 500, 500, 500))
                .setChannelId(NOTIFICATION_CHANNEL_ID)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    @SuppressLint("MissingPermission")
    fun setDestinations(destinations: List<Destination>) {
        // Store the destinations in a map against their names
        monitoredDestinations.clear()
        destinations.forEach { monitoredDestinations[it.name] = it }

        val requestBuilder = GeofencingRequest.Builder()
        destinations
                .forEach {
                    val geofence = Geofence.Builder()
                            .setCircularRegion(it.latitude, it.longitude, DEFAULT_RADIUS)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setNotificationResponsiveness(5000)
                            .setRequestId(it.name)
                            .setLoiteringDelay(5000)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build()

                    requestBuilder.addGeofence(geofence)

                    Timber.d("Geofence ${it.name} added to request")
                }

        geofencingClient.addGeofences(
                requestBuilder
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .build(), geoPendingIntent)

        Timber.d("Geofence request added with ${destinations.size} region(s)")
    }
}

class DestinationBinder(val destinationService: DestinationService) : Binder()