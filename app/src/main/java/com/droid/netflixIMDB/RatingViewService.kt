package com.droid.netflixIMDB

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView


class RatingViewService : Service() {

    private val TAG: String = javaClass.simpleName
    private var mWindowManager: WindowManager? = null
    private var mChatHeadView: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.rating_view, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP

        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager?
        mWindowManager?.addView(mChatHeadView, params)

        val closeButton = mChatHeadView?.findViewById(R.id.ivClose) as ImageView
        closeButton.setOnClickListener {
            stopSelf()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (mChatHeadView != null) mWindowManager?.removeView(mChatHeadView)
    }

}
