package com.droid.netflixIMDB.reader

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.ContextUtils
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.areNotificationsEnabled
import com.droid.netflixIMDB.notifications.NotificationManager
import com.droid.netflixIMDB.toastLong
import com.droid.netflixIMDB.util.Prefs

class YoutubeReader : Reader() {

    private val TAG: String = javaClass.simpleName
    private val SKIP_BUTTON = "com.google.android.youtube:id/skip_ad_button"
    private var timer: CountDownTimer? = null

    companion object {
        var isTimerRunning = false
    }

    override fun analyze(node: AccessibilityNodeInfo, context: Context) {
        val nodeTitle = node.findAccessibilityNodeInfosByViewId(SKIP_BUTTON)
        nodeTitle.forEach { child ->
            child?.run {
                if (this.isClickable && this.isVisibleToUser) {
                    isTimerRunning = true

                    ContextUtils.setAppLocale(context, Prefs.getLanguageSelected().toString())

                    Application.mixpanel.track("Skipped ad successfully")

                    if (Prefs.hasExceedLimit()) {
                        Application.mixpanel.track("exceeded limit: ${Prefs.getSkipCount()}")

                        if (context.areNotificationsEnabled()) {
                            Application.mixpanel.track("notifications enabled, showing premium hint")
                            NotificationManager.createPremiumPushNotification(context)
                        } else {
                            Application.mixpanel.track("notifications disabled, showing toast hint")
                            context.toastLong(context.getString(R.string.exhausted_trail_hint))
                        }
                        Log.i(TAG, "Exceeded max events, user is not premium")
                        return
                    }

                    this.performAction(AccessibilityNodeInfo.ACTION_CLICK)

                    Prefs.incrementSkipCount()

                    startTimer()

                    if (!Prefs.hasShownYoutubeHint()) {
                        Application.mixpanel.track("showed first time skip hint")
                        context.toastLong(context.getString(R.string.first_ad_skip_hint))
                        Prefs.setHasShownYoutubeHint(true)
                    }
                }
            }
        }
    }

    private fun startTimer() {
        if (timer == null) {
            isTimerRunning = true
            timer = object : CountDownTimer(4000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.i(TAG, "Youtube timer running")
                }

                override fun onFinish() {
                    cancel()
                    timer = null
                    isTimerRunning = false
                    Log.i(TAG, "Youtube timer finished")
                }
            }
            timer?.start()
        }
    }
}