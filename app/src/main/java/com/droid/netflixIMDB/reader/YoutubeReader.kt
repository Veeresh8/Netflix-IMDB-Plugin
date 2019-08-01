package com.droid.netflixIMDB.reader

import android.os.CountDownTimer
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.Payload
import com.droid.netflixIMDB.ResponsePayload
import com.droid.netflixIMDB.ratingView.RatingViewRenderer
import com.droid.netflixIMDB.util.Prefs

class YoutubeReader : Reader() {
    private val TAG: String = javaClass.simpleName
    private val SKIP_BUTTON = "com.google.android.youtube:id/skip_ad_button"
    private var timer: CountDownTimer? = null
    private var ratingView: RatingViewRenderer? = null

    companion object {
        var isTimerRunning = false
    }

    override fun payload(node: AccessibilityNodeInfo): Payload {

        if (Prefs.hasExceedLimit()) {
            Log.i(TAG, "Exceeded max events, user is not premium")
            return Payload()
        }

        val nodeTitle = node.findAccessibilityNodeInfosByViewId(SKIP_BUTTON)
        nodeTitle.forEach { child ->
            child?.run {
                if (this.isClickable && this.isVisibleToUser) {
                    isTimerRunning = true
                    this.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    val payloadCount = Prefs.getPayloadCount()
                    val plus = payloadCount?.youtube?.plus(1)
                    plus?.run {
                        payloadCount.youtube = plus
                    }
                    Log.i(TAG, "Payload count ${payloadCount.toString()}")
                    payloadCount?.let { Prefs.savePayloadCount(it) }
                    startTimer()

                    val hasShownYoutubeHint = Prefs.hasShownYoutubeHint()
                    hasShownYoutubeHint?.run {
                        if (!this) {
                            Log.i(TAG, "Showing Youtube hint")
                            initRatingView()
                            Prefs.setHasShownYoutubeHint(true)
                        }
                    }
                    return Payload()
                }
            }
        }
        return Payload()
    }

    private fun initRatingView() {
        val responsePayload =
            ResponsePayload("", "Yay! We just skipped an ad", "", "")
        ratingView = RatingViewRenderer()
        val applicationContext = Application.instance?.applicationContext
        applicationContext?.run {
            ratingView?.init(this, true)
        }
        ratingView?.showRating(responsePayload)
        Log.i(TAG, "Showing Youtube rating hint")
    }

    private fun startTimer() {
        if (timer == null) {
            isTimerRunning = true
            timer = object : CountDownTimer(RatingViewRenderer.timeout * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.i(TAG, "Youtube timer running")
                }

                override fun onFinish() {
                    cancel()
                    timer = null
                    isTimerRunning = false
                    removeRatingView()
                    Log.i(TAG, "Youtube timer finished")
                }
            }
            timer?.start()
        }
    }

    private fun removeRatingView() {
        ratingView?.removeRatingView()
    }
}