package com.droid.netflixIMDB

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.droid.netflixIMDB.reader.Reader
import com.droid.netflixIMDB.reader.YoutubeReader
import com.droid.netflixIMDB.util.ReaderConstants
import org.json.JSONObject

class ReaderService : AccessibilityService() {

    private val TAG = javaClass.simpleName
    private val readers = HashMap<String, Reader>()

    companion object {
        var isConnected: Boolean = false
        var INSTANCE: ReaderService? = null
    }

    override fun onCreate() {
        super.onCreate()
        initReaders()
        INSTANCE = this
    }

    private fun initReaders() {
        readers.clear()
        readers[ReaderConstants.YOUTUBE] = YoutubeReader()
    }

    override fun onInterrupt() {
        Log.i(TAG, "Accessibility service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Accessibility service un-bind")
        isConnected = false
        return super.onUnbind(intent)
    }

    override fun onServiceConnected() {
        Log.i(TAG, "Accessibility service connected")
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.packageNames = ReaderConstants.supportedPackages.toTypedArray()
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN
        info.notificationTimeout = 100

        this.serviceInfo = info

        isConnected = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            event.source?.let { source ->
                if (source.packageName == null) {
                    Log.i(TAG, "Package name was null, returning!")
                    return
                }

                if (!ReaderConstants.supportedPackages.contains(source.packageName)) {
                    Log.i(TAG, "Not handling event from " + source.packageName)
                    return
                }

                if (source.packageName == ReaderConstants.YOUTUBE && YoutubeReader.isTimerRunning) {
                    Log.i(TAG, "Not handling event from Youtube when timer is running")
                    return
                }

                val reader = readers[source.packageName]
                reader?.analyze(source, this)
            }

        } catch (exception: Exception) {
            Application.mixpanel.track("reader_exception", JSONObject(exception.toString()))
            Log.d(TAG, "Exception in on event: ${exception.message}")
        }
    }
}