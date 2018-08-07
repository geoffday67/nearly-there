package uk.co.sullenart.nearlythere

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import io.reactivex.Single
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL_ID = "nearly-there-id"
private const val NOTIFICATION_ID = 1

class NotificationService: Service() {
    companion object {
        fun start(context: Context): Single<NotificationBinder> =
                Single.create { emitter ->
                    Intent(context, NotificationService::class.java).let {
                        context.bindService(it, object : ServiceConnection {
                            override fun onServiceDisconnected(name: ComponentName) {
                                emitter.onError(Exception("Error connecting to notification service"))
                            }

                            override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
                                emitter.onSuccess(binder as NotificationBinder)
                            }
                        }, Context.BIND_AUTO_CREATE)
                    }
                }
    }



    override fun onBind(intent: Intent?): IBinder = NotificationBinder(this)

}

class NotificationBinder(val notificationService: NotificationService) : Binder()