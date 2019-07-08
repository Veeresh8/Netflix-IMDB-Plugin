package com.droid.netflixIMDB

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException


@Suppress("IMPLICIT_CAST_TO_ANY")
class ReaderService : AccessibilityService() {


    private val TAG: String = javaClass.simpleName
    private val NETFLIX_PACKAGE_NAME = "com.netflix.mediaclient"
    private val NETFLIX_TITLE_ID = "com.netflix.mediaclient:id/video_details_title"
    private val NETFLIX_YEAR_ID = "com.netflix.mediaclient:id/video_details_basic_info_year"
    private val NETFLIX_MOVIE_SERIES_ID = "com.netflix.mediaclient:id/video_details_basic_info_num_seasons_or_duration"

    private var hasTitle = false
    private var hasYear = false
    private var hasTypeDetails = false

    private var title: String? = null
    private var year: String? = null
    private var type: String? = null
    private var lastTitleRequested: String? = null
    private var lastYearRequested: String? = null

    private var response: Response<OMDBResponse>? = null

    private var mWindowManager: WindowManager? = null
    private var mRatingView: View? = null
    private var timer: CountDownTimer? = null

    private lateinit var tvTitle: TextView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvRating: TextView

    companion object {
        var isConnected: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        initRatingView()
    }


    private fun initRatingView() {
        mRatingView = LayoutInflater.from(this).inflate(R.layout.rating_view, null)

        val layoutFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelOffset(R.dimen.rating_view_width),
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP

        mWindowManager = getSystemService(Service.WINDOW_SERVICE) as WindowManager?

        val closeButton = mRatingView?.findViewById(R.id.ivClose) as ImageView
        tvTitle = mRatingView?.findViewById(R.id.tvTitle) as TextView
        tvRating = mRatingView?.findViewById(R.id.tvRating) as TextView

        closeButton.setOnClickListener {
            removeRatingView()
        }
    }


    private fun removeRatingView() {
        Log.d(TAG, "Removing rating view")
        mRatingView?.let {
            if (mRatingView?.windowToken != null)
                mWindowManager?.removeView(mRatingView)
        }
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

        info.packageNames = arrayOf(NETFLIX_PACKAGE_NAME)

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
            hasTypeDetails = false
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

        val nodeMovieSeriesDetails = event.source.findAccessibilityNodeInfosByViewId(NETFLIX_MOVIE_SERIES_ID)
        nodeMovieSeriesDetails.forEach { child ->
            child?.let {
                if (!hasTypeDetails) {
                    type = child.text as String?
                    type?.let {
                        if (it.toLowerCase().contains("season"))
                            type = "series"
                        else
                            type = null
                    }
                    hasTypeDetails = true
                }
            }
        }

        val netflixPayload = NetflixPayload(title, year)

        if (netflixPayload.title.equals(lastTitleRequested, true) &&
            netflixPayload.year.equals(lastYearRequested, true)
        ) {
            Log.d(TAG, "Already requested ${netflixPayload.title} - ${netflixPayload.year}")
            return
        }

        netflixPayload.title?.let { title ->
            try {
                fetchRating(title)
            } catch (exception: Exception) {
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
        }
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

    private fun fetchRating(title: String) {
        runBlocking(Dispatchers.IO) {
            Log.i(TAG, "Requesting rating for title $title -  $year - $type")

            if (type == null) {
                response = NetworkManager.getInstance()?.getRatingAsync(title, null, year)?.await()
            } else {
                type?.let {
                    if (it == "series" && year != null) {
                        response = NetworkManager.getInstance()?.getRatingAsync(title, type, null)?.await()
                    }
                }
            }

            response?.let { response ->
                if (response.isSuccessful) {
                    when (response.code()) {
                        200 -> {

                            val rating = response.body()?.imdbRating

                            lastTitleRequested = this@ReaderService.title
                            lastYearRequested = year

                            Log.d(TAG, "Title: $lastTitleRequested - Year: $lastYearRequested - Rating: $rating")

                            val messageEvent =
                                MessageEvent(rating, lastTitleRequested, lastYearRequested, response.body()?.Type)

                            showRatingInfo(messageEvent)
                        }
                        500 -> {
                            Log.e(TAG, "OMDB server error ${response.message()}")
                            showGenericErrorToast()
                        }
                        else -> {
                            Log.e(TAG, "Failed to fetch rating for ${this@ReaderService.title} - ${response.message()}")
                            showGenericErrorToast()
                        }
                    }
                } else {
                    Log.e(TAG, "Response was not successful ${response.message()}")
                    showGenericErrorToast()
                }
            }
        }
    }

    private fun showRatingInfo(event: MessageEvent) {
        Handler(Looper.getMainLooper()).post {
            setRatingInfo(event)
        }
    }

    private fun setRatingInfo(event: MessageEvent) {
        if (::tvRating.isInitialized && ::tvTitle.isInitialized) {

            if (mRatingView?.windowToken == null)
                mWindowManager?.addView(mRatingView, params)

            var year = event.year
            var rating = event.rating
            val type = event.type

            rating = rating ?: "NA"
            year = year ?: ""

            if (year.isNotEmpty()) {
                year = "($year)"
            }

            type?.let {
                if (it.toLowerCase() == "series") {
                    year = "(Series)"
                }
            }

            tvRating.text = rating
            tvTitle.text = "${event.title} $year"

            timer?.let {
                it.cancel()
                timer = null
                Log.d(TAG, "Reset timer")
            }

            if (timer == null) {
                timer = object : CountDownTimer(4000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.d(TAG, "On tick $millisUntilFinished")
                    }

                    override fun onFinish() {
                        removeRatingView()
                    }
                }
                timer?.start()
            }
        } else {
            Log.e(TAG, "Views not initialized")
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