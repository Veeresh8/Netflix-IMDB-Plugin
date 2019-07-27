package com.droid.netflixIMDB.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationToCancel = intent?.getIntExtra("notification_cancel_id", 0)
        notificationToCancel?.run {
            if (this != 0) {
                val mNotificationManager =
                    context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                mNotificationManager?.cancel(this)
            }
        }
    }
}