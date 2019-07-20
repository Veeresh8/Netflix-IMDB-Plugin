package com.droid.netflixIMDB.ratingView

import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.ResponsePayload
import com.droid.netflixIMDB.util.ColorPrefs

class RatingViewRenderer {

    private val TAG: String = javaClass.simpleName

    private var mWindowManager: WindowManager? = null
    private var mRatingView: View? = null
    private var timer: CountDownTimer? = null
    private var userClosedView: Boolean = false

    private lateinit var tvTitle: TextView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvRating: TextView
    private lateinit var closeButton: ImageView
    private lateinit var constraintLayout: ConstraintLayout

    fun init(context: Context) {
        mRatingView = LayoutInflater.from(context).inflate(R.layout.rating_view, null)

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

        closeButton.setOnClickListener {
            userClosedView = true
            removeRatingView()
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

            if (mRatingView?.windowToken == null) {
                try {
                    mWindowManager?.addView(mRatingView, params)
                } catch (exception: Exception) {
                    Log.e(TAG, "Exception adding view to window manager: ${exception.message}")
                }
            }

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

            val viewTimeout = ColorPrefs.getViewTimeout()

            if (timer == null) {
                timer = object : CountDownTimer(viewTimeout?.toLong()!! * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.d(TAG, "On tick $millisUntilFinished")
                        if (mRatingView?.windowToken == null && !userClosedView) {
                            mWindowManager?.addView(mRatingView, params)
                            Log.i(TAG, "Rating view was removed, adding again!")
                        }
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

    private fun checkForColorPrefs() {
        val titleColor = ColorPrefs.getTitleColor()
        val backgroundColor = ColorPrefs.getBackgroundColor()
        val iconColor = ColorPrefs.getIconColor()

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