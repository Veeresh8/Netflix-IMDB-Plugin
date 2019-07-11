package com.droid.netflixIMDB

import android.view.accessibility.AccessibilityNodeInfo

class HotstarReader : Reader {

    private val HOTSTAR_TITLE_ID = "in.startv.hotstar:id/metadata_main_title"
    private val HOTSTAR_TYPE_ID = "in.startv.hotstar:id/metadata_info"

    override fun getTitle(node: AccessibilityNodeInfo): String? {
        val nodeTitle = node.findAccessibilityNodeInfosByViewId(HOTSTAR_TITLE_ID)
        nodeTitle.forEach { child ->
            child?.run {
                return child.text as String?
            }
        }
        return null
    }

    override fun getType(node: AccessibilityNodeInfo): String? {
        val nodeMovieSeriesDetails = node.findAccessibilityNodeInfosByViewId(HOTSTAR_TYPE_ID)
        nodeMovieSeriesDetails.forEach { child ->
            child?.run {
                var type = text as String?
                type?.run {
                    if (toLowerCase().contains("season"))
                        type = "series"
                    else if (toLowerCase().contains("•"))
                        type = subSequence(0, 4).toString()
                }
                return type
            }
        }
        return null
    }
}