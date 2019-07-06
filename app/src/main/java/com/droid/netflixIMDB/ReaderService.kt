package com.droid.netflixIMDB

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import android.os.Looper
import org.greenrobot.eventbus.EventBus


@Suppress("IMPLICIT_CAST_TO_ANY")
class ReaderService : AccessibilityService() {

    private val TAG: String = javaClass.simpleName
    private val NETFLIX_PACKAGE_NAME = "com.netflix.mediaclient"
    private val NETFLIX_TITLE_ID = "com.netflix.mediaclient:id/video_details_title"
    private val NETFLIX_YEAR_ID = "com.netflix.mediaclient:id/video_details_basic_info_year"

    private var hasTitle = false
    private var hasYear = false

    private var title: String? = null
    private var year: String? = null
    private var lastTitleRequested: String? = null

    override fun onInterrupt() {
        Log.i(TAG, "Accessibility service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Accessibility service un-bind")
        return super.onUnbind(intent)
    }

    override fun onServiceConnected() {
        Log.i(TAG, "Accessibility service connected")

        var info = AccessibilityServiceInfo()

        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK

        info.packageNames = arrayOf(NETFLIX_PACKAGE_NAME)

        info.feedbackType = AccessibilityServiceInfo.DEFAULT

        info.notificationTimeout = 100

        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            Log.i(TAG, "Null AccessibilityEvent")
            return
        }

        if (event.source == null) {
            Log.i(TAG, "Event source was NULL")
            return
        }

        if (event.source.packageName != NETFLIX_PACKAGE_NAME) {
            Log.i(TAG, "Not handling event from " + event.source.packageName)
            return
        }

        if (event.source == null) {
            Log.i(TAG, "Event source was NULL")
            return
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            hasTitle = false
            hasYear = false
        }

        val nodeTitle = event.source.findAccessibilityNodeInfosByViewId(NETFLIX_TITLE_ID)
        nodeTitle.forEach { child ->
            child?.let {
                if (!hasTitle) {
                    title = child.text as String?
                    hasTitle = true
                }
            }
        }

        val nodeYear = event.source.findAccessibilityNodeInfosByViewId(NETFLIX_YEAR_ID)
        nodeYear.forEach { child ->
            child?.let {
                if (!hasYear) {
                    year = child.text as String?
                    hasYear = true
                }
            }
        }

        var netflixPayload = NetflixPayload(title, year)

        if (netflixPayload.title.equals(lastTitleRequested)) {
            Log.d(TAG, "Already requested ${netflixPayload.title}")
            return
        }

        netflixPayload.title?.let { title ->
            runBlocking(Dispatchers.IO) {
                Log.d(TAG, "Requesting rating for title $title -  $year")
                val response = NetworkManager.getInstance()?.getRatingAsync(title)?.await()
                response?.let { it ->
                    if (response.isSuccessful) {
                        when (response.code()) {
                            200 -> {

                                val rating = it.body()?.imdbRating
                                val year = it.body()?.Year
                                val itemTitle = it.body()?.Title

                                lastTitleRequested = itemTitle ?: title

                                Log.d(TAG, "Title: $lastTitleRequested - Year: $year - Rating: $rating")

                                EventBus.getDefault().post(MessageEvent(rating, lastTitleRequested, year))
                            }
                            500 -> {
                                Log.e(TAG, "OMDB server error ${response.message()}")
                            }
                            else -> {
                                Log.e(TAG, "Failed to fetch rating for $title")
                            }
                        }
                    } else {
                        Log.e(TAG, "Response was not successful ${response.message()}")
                    }
                }
            }
        }
    }

    private fun checkNodeRecursively(node: AccessibilityNodeInfo) {
        node.text?.let {
            Log.d(TAG, "Text: " + node.text)
        }

        if (node.childCount > 0) {
            (0 until node.childCount).forEach { index ->
                var child = node.getChild(index)
                if (child != null && child.isVisibleToUser) {
                    checkNodeRecursively(child)
                    child.recycle()
                }
            }
        }
    }
}