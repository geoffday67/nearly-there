package uk.co.sullenart.nearlythere

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.RingtoneManager
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.android.gms.location.*
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import uk.co.sullenart.nearlythere.model.Destination

class DestinationService : Service() {
    companion object {
        private const val DESTINATIONS_EXTRA = "destinations"
        private const val DEFAULT_RADIUS = 2000.0f
        private const val REQUEST_GEOFENCE = 1
        private const val ACTION_LAUNCH = "launch"
        private const val ACTION_GEOFENCE = "geofence"
        private const val NOTIFICATION_CHANNEL_ID = "nearly-there-id"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context): Single<DestinationBinder> =
                Single.create { emitter ->
                    Intent(context, DestinationService::class.java).let {
                        it.action = ACTION_LAUNCH
                        context.bindService(it, object : ServiceConnection {
                            override fun onServiceDisconnected(name: ComponentName) {
                                emitter.onError(Exception("Error connecting to destination service"))
                            }

                            override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
                                emitter.onSuccess(binder as DestinationBinder)
                            }
                        }, Context.BIND_AUTO_CREATE)
                    }
                }
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

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle("Nearly There")
                .setContentText("Checking if you're nearly there yet")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setChannelId(NOTIFICATION_CHANNEL_ID)

        startForeground(NOTIFICATION_ID, builder.build())

        Timber.d("Destination service set to foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LAUNCH ->
                @Suppress("UNCHECKED_CAST")
                (intent.getSerializableExtra(DESTINATIONS_EXTRA) as List<Destination>?)?.let {
                    setDestinations(it)
                }

            ACTION_GEOFENCE -> {
                val event = GeofencingEvent.fromIntent(intent)
                when (event.geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_DWELL ->
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
        destinations.forEach { monitoredDestinations.put(it.name, it) }

        val requestBuilder = GeofencingRequest.Builder()
        destinations
                .forEach {
                    val geofence = Geofence.Builder()
                            .setCircularRegion(it.latitude, it.longitude, DEFAULT_RADIUS)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setNotificationResponsiveness(10000)
                            .setRequestId(it.name)
                            .setLoiteringDelay(5000)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build()

                    requestBuilder.addGeofence(geofence)

                    Timber.d("Geofence ${it.name} added to request")
                }

        geofencingClient.addGeofences(
                requestBuilder
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                        .build(), geoPendingIntent)

        Timber.d("Geofence request added with ${destinations.size} region(s)")
    }
}

class DestinationBinder(val destinationService: DestinationService) : Binder()