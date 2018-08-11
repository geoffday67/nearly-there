package uk.co.sullenart.nearlythere

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import timber.log.Timber

class DestinationService : Service() {
    companion object {
        fun start(context: Context) {
            Intent(context, DestinationService::class.java).let {
                context.startService(it)
            }
        }

        fun stop(context: Context) {
            Intent(context, DestinationService::class.java).let {
                context.stopService(it)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        startForeground(MONITORING_NOTIFICATION_ID, getMonitoringNotification())
        Timber.d("Destination service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Destination service destroyed")
    }

    private fun getMonitoringNotification(): Notification =
            NotificationCompat.Builder(this, MONITORING_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_location)
                    .setContentTitle("Nearly There")
                    .setContentText("Checking if you're nearly there yet")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setChannelId(MONITORING_CHANNEL_ID)
                    .build()
}
