package com.droid.netflixIMDB

import android.view.accessibility.AccessibilityNodeInfo

abstract class Reader {
    abstract fun getTitle(node: AccessibilityNodeInfo): String?
    abstract fun getYear(node: AccessibilityNodeInfo): String?
    abstract fun getType(node: AccessibilityNodeInfo): String?
}