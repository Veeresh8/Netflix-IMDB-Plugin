package com.droid.netflixIMDB.reader

import android.view.accessibility.AccessibilityNodeInfo
import com.droid.netflixIMDB.Payload

class HotstarReader : Reader() {

    private val HOTSTAR_TITLE_ID = "in.startv.hotstar:id/metadata_main_title"
    private val HOTSTAR_TYPE_ID = "in.startv.hotstar:id/metadata_info"

    override fun payload(node: AccessibilityNodeInfo): Payload {
        val payload = Payload()

        val nodeTitle = node.findAccessibilityNodeInfosByViewId(HOTSTAR_TITLE_ID)
        nodeTitle.forEach { child ->
            child?.run {
                payload.title = child.text as String?
            }
        }

        val nodeMovieSeriesDetails = node.findAccessibilityNodeInfosByViewId(HOTSTAR_TYPE_ID)
        nodeMovieSeriesDetails.forEach { child ->
            child?.run {
                val type = text as String?
                type?.run {
                    when {
                        toLowerCase().contains("season") -> payload.type = "series"
                        toLowerCase().contains("•") -> payload.year = subSequence(0, 4).toString()
                        else -> payload.year = type
                    }
                }
            }
        }

        if (payload.year == null)
            payload.year = ""

        return payload
    }
}