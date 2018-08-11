package uk.co.sullenart.nearlythere

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import uk.co.sullenart.nearlythere.model.Destination

class AlertManager(val context: Context) {
    companion object {
        const val ALERT_CHANNEL_NAME = "Destination alert"
        const val ALERT_CHANNEL_ID = "alert"
        const val ACTION_DELETE_ALERT = "alert_delete_action"
        const val EXTRA_DELETE_ALERT_NAME = "extra_delete_alert_name"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var nextNotificationId = 2

    init {
        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(
                    NotificationChannel(MONITORING_CHANNEL_ID, MONITORING_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            )

            notificationManager.createNotificationChannel(
                    NotificationChannel(ALERT_CHANNEL_ID, ALERT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                        lightColor = 0xFFA500
                        enableLights(true)
                        setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                                .build())
                        vibrationPattern = longArrayOf(0, 500, 500, 500, 500, 500)
                        enableVibration(true)
                    }
            )
        }
    }

    fun alertDestination(destination: Destination) {
        notificationManager.notify(nextNotificationId, getAlertNotification(destination))
        nextNotificationId++
    }

    private fun getAlertNotification(destination: Destination): Notification {
        val pendingIntent = PendingIntent.getActivity(context, nextNotificationId,
                Intent(context, MainActivity::class.java).apply {
                    action = ACTION_DELETE_ALERT
                    putExtra(EXTRA_DELETE_ALERT_NAME, destination.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }, 0)

        return NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location)
                .setContentTitle("Nearly at ${destination.name}")
                .setChannelId(ALERT_CHANNEL_ID)
                .setLights(0xFFA500, 250, 250)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(longArrayOf(0, 500, 500, 500, 500, 500))
                .setWhen(System.currentTimeMillis())
                .setUsesChronometer(true)
                .setDeleteIntent(pendingIntent)
                .build()
    }

}