package com.droid.netflixIMDB.util

import android.content.Context
import android.content.SharedPreferences
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.R


object ColorPrefs {

    private var sharedPreferences: SharedPreferences? = null

    private const val TITLE_COLOR = "title_color"
    private const val BACKGROUND_COLOR = "background_color"
    private const val ICON_COLOR = "icon_color"
    private const val VIEW_TIMEOUT = "view_timeout"

    private fun getSharedPrefs(): SharedPreferences? {
        sharedPreferences = Application.instance?.getSharedPreferences(
            Application.instance?.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )
        return sharedPreferences
    }

    fun setTitleColor(color: Int) {
        getSharedPrefs()?.run {
            edit().putInt(TITLE_COLOR, color).apply()
        }
    }

    fun setBackgroundColor(color: Int) {
        getSharedPrefs()?.run {
            edit().putInt(BACKGROUND_COLOR, color).apply()
        }
    }

    fun setViewTimeout(timeout: Int) {
        getSharedPrefs()?.run {
            edit().putInt(VIEW_TIMEOUT, timeout).apply()
        }
    }

    fun setIconColor(color: Int) {
        getSharedPrefs()?.run {
            edit().putInt(ICON_COLOR, color).apply()
        }
    }

    fun getTitleColor(): Int? {
        return getSharedPrefs()
            ?.getInt(TITLE_COLOR, 0)
    }

    fun getBackgroundColor(): Int? {
        return getSharedPrefs()
            ?.getInt(BACKGROUND_COLOR, 0)
    }

    fun getIconColor(): Int? {
        return getSharedPrefs()
            ?.getInt(ICON_COLOR, 0)
    }

    fun getViewTimeout(): Int? {
        return getSharedPrefs()
            ?.getInt(VIEW_TIMEOUT, 5)
    }
}