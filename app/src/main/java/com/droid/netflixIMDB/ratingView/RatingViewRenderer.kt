package com.droid.netflixIMDB.ratingView

import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.ReaderService
import com.droid.netflixIMDB.ResponsePayload
import com.droid.netflixIMDB.util.LaunchUtils
import com.droid.netflixIMDB.util.Prefs

class RatingViewRenderer {

    private val TAG: String = javaClass.simpleName

    private var mWindowManager: WindowManager? = null
    private var mRatingView: View? = null
    private var mBuyView: View? = null
    private var timer: CountDownTimer? = null
    private var userClosedView: Boolean = false
    private var useShortTimeOut: Boolean = false

    private lateinit var tvTitle: TextView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvRating: TextView
    private lateinit var tvBuyTitle: TextView
    private lateinit var closeButton: ImageView
    private lateinit var ivNavToApp: ImageView
    private lateinit var closeBuyButton: ImageView
    private lateinit var constraintLayout: ConstraintLayout


    companion object {
        var timeout: Long = 4
    }

    fun init(context: Context, useShortTimeOut: Boolean = false) {

        this.useShortTimeOut = useShortTimeOut

        mRatingView = LayoutInflater.from(context).inflate(R.layout.rating_view, null)

        mBuyView = LayoutInflater.from(context).inflate(R.layout.buy_floating_view, null)

        val layoutFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP

        mWindowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager?

        closeButton = mRatingView?.findViewById(R.id.ivClose) as ImageView
        tvTitle = mRatingView?.findViewById(R.id.tvTitle) as TextView
        tvRating = mRatingView?.findViewById(R.id.tvRating) as TextView
        constraintLayout = mRatingView?.findViewById(R.id.constraintLayout) as ConstraintLayout

        tvBuyTitle = mBuyView?.findViewById(R.id.tvBuyTitle) as TextView
        closeBuyButton = mBuyView?.findViewById(R.id.ivBuyClose) as ImageView
        ivNavToApp = mBuyView?.findViewById(R.id.ivNavToApp) as ImageView
        tvBuyTitle.isSelected = true

        closeBuyButton.setOnClickListener {
            removeBuyView()
        }

        ivNavToApp.setOnClickListener {
            removeBuyView()
            LaunchUtils.launchMainActivity()
        }

        closeButton.setOnClickListener {
            userClosedView = true
            removeRatingView()
        }
    }

    private fun removeBuyView() {
        mBuyView?.run {
            if (mBuyView?.windowToken != null) {
                try {
                    mWindowManager?.removeView(mBuyView)
                } catch (exception: Exception) {
                    Log.e(
                        TAG,
                        "Exception removing buy view to window manager: ${exception.message}"
                    )
                }
            }
        }
    }

    fun showBuyView() {
        if (mBuyView?.windowToken != null) {
            Log.i(TAG, "Buy view already showing")
            return
        }

        try {
            mWindowManager?.addView(mBuyView, params)
            Log.i(TAG, "Buy view shown")
        } catch (exception: Exception) {
            Log.e(TAG, "Exception adding buy view to window manager: ${exception.message}")
        }
    }

    fun removeRatingView() {
        Log.d(TAG, "Removing rating view")
        mRatingView?.run {
            if (mRatingView?.windowToken != null) {
                try {
                    mWindowManager?.removeView(mRatingView)
                } catch (exception: Exception) {
                    Log.e(TAG, "Exception removing view to window manager: ${exception.message}")
                }
            }
        }
    }

    fun showRating(responsePayload: ResponsePayload) {
        if (::tvRating.isInitialized && ::tvTitle.isInitialized) {

            Log.d(TAG, "Showing rating: $responsePayload")

            var year = responsePayload.year
            var rating = responsePayload.rating
            val type = responsePayload.type

            rating = rating ?: "NA"
            year = year ?: ""

            if (year.isNotEmpty()) {
                year = "($year)"
            }

            type?.run {
                if (toLowerCase() == "series") {
                    year = "(Series)"
                }
            }

            tvRating.text = rating
            tvTitle.text = "${responsePayload.title} $year"

            checkForColorPrefs()

            timer?.run {
                cancel()
                timer = null
                userClosedView = false
                Log.d(TAG, "Resetting timer")
            }

            if (useShortTimeOut) {
                timeout = 3
            } else {
                val viewTimeout = Prefs.getViewTimeout()
                viewTimeout?.run {
                    if (this > 0) {
                        timeout = this.toLong()
                    }
                }
            }

            Log.d(TAG, "Using timeout: $timeout")

            if (hasOverlayPermission()) {

                removeBuyView()

                if (mRatingView?.windowToken == null) {
                    try {
                        showRatingView()
                    } catch (exception: Exception) {
                        Log.e(TAG, "Exception adding view to window manager: ${exception.message}")
                        showRatingToast()
                    }
                }

                if (timer == null) {
                    timer = object : CountDownTimer(timeout * 1000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            Log.d(TAG, "On tick $millisUntilFinished")
                            try {
                                if (mRatingView?.windowToken == null && !userClosedView) {
                                    showRatingView()
                                }
                            } catch (exception: Exception) {
                                Log.e(
                                    TAG,
                                    "Exception adding view to window manager: ${exception.message}"
                                )
                                showRatingToast()
                            }
                        }

                        override fun onFinish() {
                            removeRatingView()
                        }
                    }
                    timer?.start()
                }
            } else {
                showRatingToast()
            }

        } else {
            Log.e(TAG, "Views not initialized")
        }
    }

    private fun showRatingToast() {
        Toast.makeText(
            ReaderService.INSTANCE,
            "${tvTitle.text} - ${tvRating.text}",
            Toast.LENGTH_LONG
        ).show()
        Log.i(TAG, "Overlay permission denied, show toast")
    }

    private fun showRatingView() {
        mWindowManager?.addView(mRatingView, params)
        Log.i(TAG, "Rating view shown")
    }

    private fun hasOverlayPermission() =
        ReaderService.INSTANCE != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(
            ReaderService.INSTANCE
        )

    private fun checkForColorPrefs() {
        val titleColor = Prefs.getTitleColor()
        val backgroundColor = Prefs.getBackgroundColor()
        val iconColor = Prefs.getIconColor()

        if (titleColor != null && titleColor != 0) {
            tvTitle.setTextColor(titleColor)
            tvRating.setTextColor(titleColor)
        }

        if (backgroundColor != null && backgroundColor != 0) {
            constraintLayout.setBackgroundColor(backgroundColor)
        }

        if (iconColor != null && iconColor != 0) {
            closeButton.setColorFilter(iconColor)
        }
    }
}