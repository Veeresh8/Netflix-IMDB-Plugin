package com.droid.netflixIMDB.reader

import android.view.accessibility.AccessibilityNodeInfo
import com.droid.netflixIMDB.Payload

class NetflixReader : Reader() {

    private val NETFLIX_TITLE_ID = "com.netflix.mediaclient:id/mini_dp_title"
    private val NETFLIX_YEAR_ID = "com.netflix.mediaclient:id/mini_dp_year"
    private val NETFLIX_MOVIE_SERIES_ID = "com.netflix.mediaclient:id/mini_dp_season_num_or_run_time"

    override fun payload(node: AccessibilityNodeInfo): Payload {
        val payload = Payload()
        val nodeTitle = node.findAccessibilityNodeInfosByViewId(NETFLIX_TITLE_ID)
        nodeTitle.forEach { child ->
            child?.run {
                payload.title = child.text as String?
            }
        }

        val nodeYear = node.findAccessibilityNodeInfosByViewId(NETFLIX_YEAR_ID)
        nodeYear.forEach { child ->
            child?.run {
                payload.year = child.text as String?
            }
        }

        val nodeMovieSeriesDetails = node.findAccessibilityNodeInfosByViewId(NETFLIX_MOVIE_SERIES_ID)
        nodeMovieSeriesDetails.forEach { child ->
            child?.run {
                var type = text as String?
                type?.run {
                    type = if (toLowerCase().contains("season"))
                        "series"
                    else
                        null
                }
                payload.type = type
            }
        }
        return payload
    }
}