package com.droid.netflixIMDB.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.Dashboard
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.util.LaunchUtils
import java.util.UUID

object NotificationManager {

    private const val BUY_PREMIUM_NOTIFICATION_ID = 1337

    fun createPremiumPushNotification(context: Context) {
        if (isNotificationBeingShown(context, BUY_PREMIUM_NOTIFICATION_ID)) {
            return
        }

        val uri = Uri.parse("content://com.android.contacts/contacts/lookup/0r1-4F314D4F2F2394F29/1234").toString()

        val otherPerson = Person.Builder()
            .setBot(false)
            .setName(context.getString(R.string.app_name))
            .setImportant(true)
            .setUri(uri)
            .build()

        val self = Person.Builder()
            .setBot(false)
            .setName(context.getString(R.string.app_name))
            .setUri(uri)
            .setImportant(true)
            .build()

        val mNotificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId =
            "${context.getString(R.string.app_name)} youtube ad skipper notification channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                channelId, context.getString(R.string.app_name), importance
            )
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val title = context.getString(R.string.exhausted_trail_hint)
        val body = context.getString(R.string.click_to_upgrade)

        val notificationWithoutImage = RemoteViews(context.packageName, R.layout.remote_notification)
        notificationWithoutImage.setTextViewText(R.id.tvTitle, title)
        notificationWithoutImage.setTextViewText(R.id.tvSubTitle, body)

        val mBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_round_fast_forward_black_48dp)
            .setOnlyAlertOnce(true)
            .addPerson(self)
            .setContentTitle(title)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.MessagingStyle(self)
                .setConversationTitle(context.getString(R.string.app_name))
                .addMessage(body, System.currentTimeMillis(), otherPerson))
            .setAutoCancel(true)
            .setCustomContentView(notificationWithoutImage)
            .setCustomHeadsUpContentView(notificationWithoutImage)
            .setCustomBigContentView(notificationWithoutImage)
            .setGroup(UUID.randomUUID().toString())
            .setVibrate(longArrayOf(1000, 1000))
            .setContentText(body)

        mBuilder.setContentIntent(getBuyIntent(context, BUY_PREMIUM_NOTIFICATION_ID))
        val notification = mBuilder.build()

        mNotificationManager.notify(BUY_PREMIUM_NOTIFICATION_ID, notification)
    }

    private fun getBuyIntent(context: Context, notificationID: Int): PendingIntent {
        val launchIntent = Intent(context, Dashboard::class.java)
        launchIntent.putExtra("notification_id", notificationID)
        return PendingIntent.getActivity(
            context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getPlayStoreIntent(context: Context): PendingIntent {
        val launchIntent = LaunchUtils.getPlaystoreIntent()
        return PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), launchIntent, PendingIntent.FLAG_IMMUTABLE
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

    private fun isNotificationBeingShown(context: Context, notificationID: Int): Boolean {
        val mNotificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notifications = mNotificationManager?.activeNotifications
            if (notifications != null) {
                for (notification in notifications) {
                    if (notification.id == notificationID) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getNotificationManager(): NotificationManager? {
        return Application.instance?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
    }
}