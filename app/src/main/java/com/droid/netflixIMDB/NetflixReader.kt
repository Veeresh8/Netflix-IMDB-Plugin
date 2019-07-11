package com.droid.netflixIMDB

import android.view.accessibility.AccessibilityNodeInfo

class NetflixReader : Reader {

    private val NETFLIX_TITLE_ID = "com.netflix.mediaclient:id/video_details_title"
    private val NETFLIX_YEAR_ID = "com.netflix.mediaclient:id/video_details_basic_info_year"
    private val NETFLIX_MOVIE_SERIES_ID = "com.netflix.mediaclient:id/video_details_basic_info_num_seasons_or_duration"

    override fun getTitle(node: AccessibilityNodeInfo): String? {
        val nodeTitle = node.findAccessibilityNodeInfosByViewId(NETFLIX_TITLE_ID)
        nodeTitle.forEach { child ->
            child?.run {
                return child.text as String?
            }
        }
        return null
    }

    override fun getYear(node: AccessibilityNodeInfo): String? {
        val nodeYear = node.findAccessibilityNodeInfosByViewId(NETFLIX_YEAR_ID)
        nodeYear.forEach { child ->
            child?.run {
                return child.text as String?
            }
        }
        return null
    }

    override fun getType(node: AccessibilityNodeInfo): String? {
        val nodeMovieSeriesDetails = node.findAccessibilityNodeInfosByViewId(NETFLIX_MOVIE_SERIES_ID)
        nodeMovieSeriesDetails.forEach { child ->
            child?.run {
                var type = text as String?
                type?.run {
                    if (toLowerCase().contains("season"))
                        type = "series"
                    else
                        type = null
                }
                return type
            }
        }
        return null
    }
}