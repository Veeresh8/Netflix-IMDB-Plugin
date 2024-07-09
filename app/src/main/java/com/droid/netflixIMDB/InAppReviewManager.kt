package com.droid.netflixIMDB

import android.content.Context
import android.content.SharedPreferences
import com.droid.netflixIMDB.util.Prefs
import java.util.concurrent.TimeUnit

class InAppReviewManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("InAppReviewPrefs", Context.MODE_PRIVATE)

    fun shouldShowReview(): Boolean {
        val currentTime = System.currentTimeMillis()
        val firstLaunchTime = prefs.getLong("firstLaunchTime", 0)
        val lastReviewTime = prefs.getLong("lastReviewTime", 0)
        val launchCount = prefs.getInt("launchCount", 0)

        // If it's the first launch, save the current time
        if (firstLaunchTime == 0L) {
            prefs.edit().putLong("firstLaunchTime", currentTime).apply()
            prefs.edit().putInt("launchCount", 1).apply()
            return false
        }

        // Increment launch count
        val newLaunchCount = launchCount + 1
        prefs.edit().putInt("launchCount", newLaunchCount).apply()

        // Check if at least 2 days have passed since the last review (or first launch if no review yet)
        val daysSinceLastReview = TimeUnit.MILLISECONDS.toDays(currentTime - (lastReviewTime.takeIf { it != 0L } ?: firstLaunchTime))

        // Determine if we should show the review
        val shouldShow = newLaunchCount >= 4 && daysSinceLastReview >= 2 && Prefs.getSkipCount() > 3

        if (shouldShow) {
            // Update the last review time
            prefs.edit().putLong("lastReviewTime", currentTime).apply()
        }

        return shouldShow
    }
}