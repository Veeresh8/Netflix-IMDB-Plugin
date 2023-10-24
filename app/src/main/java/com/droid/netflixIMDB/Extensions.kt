package com.droid.netflixIMDB

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val DEFAULT_DEBOUNCE_INTERVAL_MS = 1000L


fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun Context.areNotificationsEnabled(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}

fun Context.toast(message: String) {
    GlobalScope.launch (Dispatchers.Main.immediate) {
        Toast.makeText(Application.instance, message, Toast.LENGTH_SHORT).show()
    }
}

fun Context.toastLong(message: String) {
    GlobalScope.launch (Dispatchers.Main.immediate) {
        Toast.makeText(Application.instance, message, Toast.LENGTH_LONG).show()
    }
}

fun ProgressBar.updateProgressAndAnimate(progress: Int) {
    val progressAnimator = ObjectAnimator.ofInt(this, "progress", progress)
    progressAnimator.duration = 500 // animation duration in milliseconds
    progressAnimator.interpolator = DecelerateInterpolator()
    progressAnimator.start()
}

private fun Context.isDarkModeOn(): Boolean {
    val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
}


fun View.setOnDebouncedClickListener(
    interval: Long = DEFAULT_DEBOUNCE_INTERVAL_MS,
    onClick: (View) -> Unit
) {
    var lastClickTime = 0L
    val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce)
    bounceAnimation.setAnimationListener(
        object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                onClick(this@setOnDebouncedClickListener)
            }
            override fun onAnimationRepeat(animation: Animation) {}
        }
    )
    setOnClickListener {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime > interval) {
            lastClickTime = currentTime
            startAnimation(bounceAnimation)
        }
    }
}
