package com.droid.netflixIMDB.reader

import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.droid.netflixIMDB.Payload
import com.droid.netflixIMDB.util.Prefs

class YoutubeReader : Reader() {
    private val TAG: String = javaClass.simpleName
    private val SKIP_BUTTON = "com.google.android.youtube:id/skip_ad_button"

    override fun payload(node: AccessibilityNodeInfo): Payload {
        val nodeTitle = node.findAccessibilityNodeInfosByViewId(SKIP_BUTTON)
        nodeTitle.forEach { child ->
            child?.run {
                if (this.isClickable && this.isVisibleToUser) {
                    this.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Handler().postDelayed({
                        val payloadCount = Prefs.getPayloadCount()
                        val plus = payloadCount?.youtube?.plus(1)
                        plus?.run {
                            payloadCount.youtube = plus
                        }
                        Log.i(TAG, "Payload count ${payloadCount.toString()}")
                        payloadCount?.let { Prefs.savePayloadCount(it) }
                    }, 2000)
                }
            }
        }
        return Payload()
    }
}