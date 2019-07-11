package com.droid.netflixIMDB

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

class RatingViewRenderer {

    private val TAG: String = javaClass.simpleName

    private var mWindowManager: WindowManager? = null
    private var mRatingView: View? = null
    private var timer: CountDownTimer? = null

    private lateinit var tvTitle: TextView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvRating: TextView

    fun init(context: Context) {
        mRatingView = LayoutInflater.from(context).inflate(R.layout.rating_view, null)

        val layoutFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            context.resources.getDimensionPixelOffset(R.dimen.rating_view_width),
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP

        mWindowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager?

        val closeButton = mRatingView?.findViewById(R.id.ivClose) as ImageView
        tvTitle = mRatingView?.findViewById(R.id.tvTitle) as TextView
        tvRating = mRatingView?.findViewById(R.id.tvRating) as TextView

        closeButton.setOnClickListener {
            removeRatingView()
        }
    }

    fun removeRatingView() {
        Log.d(TAG, "Removing rating view")
        mRatingView?.run {
            if (mRatingView?.windowToken != null)
                mWindowManager?.removeView(mRatingView)
        }
    }

    fun showRating(responsePayload: ResponsePayload) {
        if (::tvRating.isInitialized && ::tvTitle.isInitialized) {

            if (mRatingView?.windowToken == null)
                mWindowManager?.addView(mRatingView, params)

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

            timer?.run {
                cancel()
                timer = null
                Log.d(TAG, "Resetting timer")
            }

            if (timer == null) {
                timer = object : CountDownTimer(4500, 1000) {
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
}