package com.droid.netflixIMDB.reader

import android.view.accessibility.AccessibilityNodeInfo
import com.droid.netflixIMDB.Payload

abstract class Reader {
    abstract fun payload(node: AccessibilityNodeInfo): Payload
}