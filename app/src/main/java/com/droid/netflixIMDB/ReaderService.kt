package com.droid.netflixIMDB

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.droid.netflixIMDB.ratingView.RatingViewRenderer
import com.droid.netflixIMDB.reader.HotstarReader
import com.droid.netflixIMDB.reader.NetflixReader
import com.droid.netflixIMDB.reader.Reader
import com.google.firebase.analytics.FirebaseAnalytics
import java.net.SocketTimeoutException
import java.net.UnknownHostException


@Suppress("IMPLICIT_CAST_TO_ANY")
class ReaderService : AccessibilityService() {


    private val TAG: String = javaClass.simpleName

    private var title: String? = null
    private var year: String? = null
    private var type: String? = null

    private val readers = HashMap<String, Reader>()

    private var ratingView: RatingViewRenderer? = null

    private var firebaseAnalytics: FirebaseAnalytics? = null

    companion object {
        var isConnected: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        initReaders()
        initRatingView()
        initAnalytics()
    }

    private fun initAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    private fun initReaders() {
        readers.clear()
        readers[ReaderConstants.NETFLIX] = NetflixReader()
        readers[ReaderConstants.HOTSTAR] = HotstarReader()
    }

    private fun initRatingView() {
        ratingView = RatingViewRenderer()
        ratingView?.init(this)
    }

    private fun removeRatingView() {
        ratingView?.removeRatingView()
    }

    override fun onInterrupt() {
        Log.i(TAG, "Accessibility service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Accessibility service un-bind")
        isConnected = false
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeRatingView()
    }

    override fun onServiceConnected() {
        Log.i(TAG, "Accessibility service connected")

        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.packageNames = ReaderConstants.supportedPackages.toTypedArray()
        info.feedbackType = AccessibilityServiceInfo.DEFAULT
        info.notificationTimeout = 100

        this.serviceInfo = info

        isConnected = true
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

        if (event.source.packageName == null) {
            Log.i(TAG, "PackageName was NULL")
            return
        }

        if (!ReaderConstants.supportedPackages.contains(event.source.packageName)) {
            Log.i(TAG, "Not handling event from " + event.source.packageName)
            return
        }

        val reader = readers[event.source.packageName]

        val readerPayload = reader?.payload(event.source)

        title = readerPayload?.title
        year = readerPayload?.year
        type = readerPayload?.type

        val payload = Payload(title, year, type)

        Log.i(TAG, "Scraped item: $payload")

        if (payload.title == null || (payload.year == null && payload.type == null)) {
            Log.i(TAG, "No title request")
            return
        }

        if (payload.title.equals(RatingRequester.lastTitle, true) &&
            payload.year.equals(RatingRequester.lastYear, true)
        ) {
            Log.i(TAG, "Already requested $payload")
            return
        }

        val bundle = Bundle()
        bundle.putString(ReaderConstants.PACKAGE_NAME, event.source.packageName.toString())
        bundle.putString(ReaderConstants.TITLE, payload.title ?: "")
        bundle.putString(ReaderConstants.TYPE, payload.type ?: "")
        bundle.putString(ReaderConstants.YEAR, payload.year ?: "")
        firebaseAnalytics?.logEvent(ReaderConstants.SEARCH, bundle)

        RatingRequester.requestRating(payload, object : RatingRequester.RatingRequesterCallback {
            override fun onFailure(message: String) {
                showToastWithMessage(message)
            }

            override fun onSuccess(responsePayload: ResponsePayload) {
                showRating(responsePayload)
            }

            override fun onRequestException(exception: Exception) {
                when (exception) {
                    is SocketTimeoutException -> {
                        showConnectionErrorToast()
                    }
                    is UnknownHostException -> {
                        showConnectionErrorToast()
                    }
                    else -> {
                        showGenericErrorToast()
                    }
                }
            }
        })
    }

    private fun showConnectionErrorToast() {
        Toast.makeText(
            this,
            "Failed to fetch ratings, check your network connectivity",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showGenericErrorToast() {
        Toast.makeText(
            this,
            "Failed to fetch ratings, try again!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showToastWithMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showRating(responsePayload: ResponsePayload) {
        Handler(Looper.getMainLooper()).post {
            ratingView?.showRating(responsePayload)
        }
    }

    private fun checkNodeRecursively(node: AccessibilityNodeInfo) {
        node.text?.let {
            Log.d(TAG, "Text: " + node.text)
        }

        if (node.childCount > 0) {
            (0 until node.childCount).forEach { index ->
                val child = node.getChild(index)
                if (child != null && child.isVisibleToUser) {
                    checkNodeRecursively(child)
                    child.recycle()
                }
            }
        }
    }
}