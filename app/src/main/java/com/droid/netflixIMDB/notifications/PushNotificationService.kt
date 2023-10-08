//package com.droid.netflixIMDB.notifications
//
//import android.util.Log
//import com.droid.netflixIMDB.util.Prefs
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
//
//
//class PushNotificationService : FirebaseMessagingService() {
//    private val TAG: String = this.javaClass.simpleName
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        try {
//            if (remoteMessage == null) {
//                Log.d(TAG, "Push notification with NULL data")
//                return
//            }
//
//            if (remoteMessage.notification?.title != null) {
//                Log.d(
//                    TAG,
//                    "Push notification extra-data: ${remoteMessage.notification?.title} " +
//                            "|| ${remoteMessage.notification?.body}"
//                )
//                val title = remoteMessage.notification?.title ?: "Netflix IMDB Plugin"
//                val body = remoteMessage.notification?.body ?: ""
//                val hasPlaystoreIntent = remoteMessage.data["open_playstore"] != null
//                NotificationManager.initPushNotification(this, title, body, hasPlaystoreIntent)
//            } else if (remoteMessage.data != null) {
//                Log.d(TAG, "Push notification data: " + remoteMessage.data.toString())
//                val title = remoteMessage.data["title"] ?: "Netflix IMDB Plugin"
//                val body = remoteMessage.data["body"] ?: ""
//                val hasPlaystoreIntent = remoteMessage.data["open_playstore"] != null
//                NotificationManager.initPushNotification(this, title, body, hasPlaystoreIntent)
//            }
//        } catch (exception: Exception) {
//            Log.e(TAG, "Exception parsing push notification: ${exception.message}")
//        }
//    }
//
//    override fun onNewToken(token: String) {
//
//    }
//}