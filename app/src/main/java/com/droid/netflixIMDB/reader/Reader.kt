package com.droid.netflixIMDB.reader

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo

abstract class Reader {
    abstract fun analyze(node: AccessibilityNodeInfo, context: Context)
}