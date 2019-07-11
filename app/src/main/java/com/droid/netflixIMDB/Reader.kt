package com.droid.netflixIMDB

import android.view.accessibility.AccessibilityNodeInfo

interface Reader {
    fun getTitle(node: AccessibilityNodeInfo): String? = null
    fun getYear(node: AccessibilityNodeInfo): String? = null
    fun getType(node: AccessibilityNodeInfo): String? = null
}