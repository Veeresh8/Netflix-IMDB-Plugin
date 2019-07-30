package com.droid.netflixIMDB.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.MainActivity
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.util.LaunchUtils
import com.droid.netflixIMDB.util.Prefs


object NotificationManager {

    private lateinit var intent: Intent
    private const val NOTIFICATION_ID = 1337
    private const val NOTIFICATION_PLAYSTORE_ID = 1338

    fun initPushNotification(context: Context, title: String, body: String, hasPlayStoreIntent: Boolean = false) {
        intent = if (hasPlayStoreIntent) {
            LaunchUtils.getPlaystoreIntent()
        } else {
            Intent(context, MainActivity::class.java)
        }

        val contentIntent =
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                0
            )
        createPushNotification(
            contentIntent,
            context,
            title,
            body
        )
    }

    fun createPlayStorePushNotification(context: Context, title: String, body: String) {
        val contentIntent =
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                LaunchUtils.getPlaystoreIntent(),
                0
            )
        createPushNotification(
            contentIntent,
            context,
            title,
            body,
            hasPlayStoreIntent = true
        )
    }

    fun createLauncherPushNotification(context: Context, title: String, body: String) {

        if (isNotificationBeingShown(context) || Prefs.getIsPremiumHintShown()) {
            Log.d(javaClass.simpleName, "Notification not shown: because is being shown || isPremium user")
            return
        }

        Prefs.setIsPremiumHintShown(true)

        val contentIntent =
            PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                Intent(context, MainActivity::class.java),
                0
            )
        createPushNotification(
            contentIntent,
            context,
            title,
            body,
            true
        )
    }

    private fun createPushNotification(
        contentIntent: PendingIntent,
        context: Context,
        title: String? = Application.instance?.getString(R.string.app_name),
        body: String,
        hasBuyIntent: Boolean = false,
        hasPlayStoreIntent: Boolean = false
    ) {
        val mNotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                "${context.getString(R.string.app_name)} general channel",
                context.getString(R.string.app_name), importance
            )
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val mBuilder = NotificationCompat.Builder(
            context,
            "${context.getString(R.string.app_name)} general channel"
        )
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setOngoing(true)
            .setContentTitle(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000))
            .setContentText(body)

        if (hasBuyIntent) {
            mBuilder.addAction(0, "Go Pro", getBuyIntent(context, NOTIFICATION_ID))
            mBuilder.addAction(0, "No Thanks", getIgnoreIntent(context, NOTIFICATION_ID))
        } else if (hasPlayStoreIntent) {
            mBuilder.addAction(0, "Write Honest Review", getPlayStoreIntent(context))
            mBuilder.addAction(0, "Already Done", getIgnoreIntent(context, NOTIFICATION_PLAYSTORE_ID))
        }

        mBuilder.setContentIntent(contentIntent)
        val notification = mBuilder.build()

        if (hasBuyIntent)
            mNotificationManager.notify(NOTIFICATION_ID, notification)
        else
            mNotificationManager.notify(NOTIFICATION_PLAYSTORE_ID, notification)
    }

    private fun getBuyIntent(context: Context, notificationID: Int): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java)
        launchIntent.putExtra("notification_id", notificationID)
        return PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            0
        )
    }

    private fun getPlayStoreIntent(context: Context): PendingIntent {
        val launchIntent = LaunchUtils.getPlaystoreIntent()
        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            launchIntent,
            0
        )
    }

    private fun getIgnoreIntent(context: Context, notificationID: Int): PendingIntent {
        val ignoreIntent = Intent(context, NotificationActionReceiver::class.java)
        ignoreIntent.putExtra("notification_cancel_id", notificationID)

        return PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun isNotificationBeingShown(context: Context): Boolean {
        val mNotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mNotificationManager?.activeNotifications
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        if (notifications != null) {
            for (notification in notifications) {
                if (notification.id == NOTIFICATION_ID) {
                    return true
                }
            }
        }
        return false
    }

    fun getNotificationManager(): NotificationManager? {
        return Application.instance?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
    }
}