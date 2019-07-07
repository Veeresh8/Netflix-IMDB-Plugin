package com.droid.netflixIMDB

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class RatingRendererService : Service() {

    private val TAG: String = javaClass.simpleName
    private var mWindowManager: WindowManager? = null
    private var mRatingView: View? = null
    private var timer: CountDownTimer? = null

    private lateinit var tvTitle: TextView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var tvRating: TextView

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")

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

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager?

        val closeButton = mRatingView?.findViewById(R.id.ivClose) as ImageView
        tvTitle = mRatingView?.findViewById(R.id.tvTitle) as TextView
        tvRating = mRatingView?.findViewById(R.id.tvRating) as TextView

        closeButton.setOnClickListener {
            removeRatingView()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessage(event: MessageEvent) {
        if (::tvRating.isInitialized && ::tvTitle.isInitialized) {

            if (mRatingView?.windowToken == null)
                mWindowManager?.addView(mRatingView, params)

            var year = event.year
            var rating = event.rating

            rating = rating ?: "NA"
            year = year ?: ""

            if (year.isNotEmpty()) {
                year = "($year)"
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        removeRatingView()
    }

    private fun removeRatingView() {
        Log.d(TAG, "Removing rating view")
        mRatingView?.let {
            if (mRatingView?.windowToken != null)
                mWindowManager?.removeView(mRatingView)
        }
    }
}
