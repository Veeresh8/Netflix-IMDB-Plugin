package com.droid.netflixIMDB

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class RatingViewService : Service() {


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

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        EventBus.getDefault().register(this)

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

            val aniSlide = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
            mRatingView?.startAnimation(aniSlide)

            val year = event.year

            tvRating.text = event.rating
            tvTitle.text = String.format(event.title + " " + year)

            if (timer != null) {
                timer?.cancel()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun mustDismissView(event: RemoveRatingViewEvent) {
        if (event.mustRemoveView) {
            removeRatingView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
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
