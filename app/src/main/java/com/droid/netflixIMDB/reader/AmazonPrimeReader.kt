package com.droid.netflixIMDB.reader

import android.view.accessibility.AccessibilityNodeInfo
import com.droid.netflixIMDB.Payload
import java.util.regex.Pattern

class AmazonPrimeReader : Reader() {
    private val PRIME_TITLE_ID = "com.amazon.avod.thirdpartyclient:id/header_title_text"
    private val IMDB_TITLE_ID = "com.amazon.avod.thirdpartyclient:id/header_metadata_container"
    private val yearPattern = Pattern.compile("([0-9]){4}")
    private val bracketsPattern = Pattern.compile("[(]([0-9]){4}(.*?\\)\$)")

    override fun payload(node: AccessibilityNodeInfo): Payload {
        val payload = Payload()

        val nodeTitle = node.findAccessibilityNodeInfosByViewId(PRIME_TITLE_ID)
        nodeTitle.forEach { child ->
            child?.run {
                payload.title = child.text as String?
                val bracketsMatcher = bracketsPattern.matcher(payload.title)
                if (bracketsMatcher.find()) {
                    payload.title = payload.title?.replace(bracketsMatcher.group(), "")
                }
            }
        }

        val nodeIMDb = node.findAccessibilityNodeInfosByViewId(IMDB_TITLE_ID)
        nodeIMDb.forEach { child ->
            child?.run {
                val meta = child.contentDescription as String?
                if (meta != null && meta.toLowerCase().contains("imdb", true)) {
                    payload.title = null
                } else {
                    val yearMatcher = yearPattern.matcher(meta)
                    if (yearMatcher.find()) {
                        payload.year = yearMatcher.group()
                    }
                }
            }
        }
        return payload
    }
}