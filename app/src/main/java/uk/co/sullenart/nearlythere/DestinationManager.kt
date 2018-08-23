package uk.co.sullenart.nearlythere

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import timber.log.Timber
import uk.co.sullenart.nearlythere.model.Destination

class DestinationManager (val context: Context) {
    companion object {
        private const val DEFAULT_RADIUS = 5000.0f
        private const val PENDING_GEOFENCE = 1
        private const val ACTION_GEOFENCE = "geofence"
    }

    private val monitoredDestinations = HashMap<String, Destination>()
    private val geofencingClient = LocationServices.getGeofencingClient(context)

    private val enteredProcessor: PublishProcessor<Destination> = PublishProcessor.create()
    val enteredDestinations: Flowable<Destination>
        get() = enteredProcessor.share()


    fun setDestinations(intent: Intent, destinations: List<Destination>) {
        // TODO Stop any existing geofences

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

        // Create a pending intent for the supplied intent
        val pendingIntent = PendingIntent.getActivity(context, PENDING_GEOFENCE,
                intent.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    action = ACTION_GEOFENCE
                },
                PendingIntent.FLAG_UPDATE_CURRENT)

        try {
            geofencingClient.addGeofences(
                    requestBuilder
                            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                            .build(), pendingIntent)

            Timber.d("Geofence request added with ${destinations.size} region(s)")
        } catch (e: SecurityException) {
            Timber.e(e)
        }
    }

    fun cancelAll() {
        geofencingClient.removeGeofences(monitoredDestinations.keys.toList())
    }

    fun handleNewIntent(intent: Intent) {
        when (intent.action) {
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
                        }
                }
            }
        }
    }
}