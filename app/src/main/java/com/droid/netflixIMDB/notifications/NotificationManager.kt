package com.droid.netflixIMDB.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.droid.netflixIMDB.MainActivity
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.util.LaunchUtils

object NotificationManager {

    private const val channelID = "Netflix IMDB Plugin Channel"
    private const val name = "Netflix IMDB Plugin"
    private lateinit var intent: Intent

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
            body,
            hasPlayStoreIntent
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
            true
        )
    }

    private fun createPushNotification(
        contentIntent: PendingIntent,
        context: Context,
        title: String? = "Netflix IMDB Plugin",
        body: String, addPlayStoreButton: Boolean = false
    ) {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                channelID,
                name, importance
            )
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val mBuilder = NotificationCompat.Builder(
            context,
            channelID
        )
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
            )
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000))
            .setContentText(body)

//        if (addPlayStoreButton) {
//            mBuilder.addAction(R.drawable.round_stars_24px, "DELIVER HONEST RATING", contentIntent)
//        }

        mBuilder.setContentIntent(contentIntent)
        val notification = mBuilder.build()
        mNotificationManager.notify(0, notification)
    }
}