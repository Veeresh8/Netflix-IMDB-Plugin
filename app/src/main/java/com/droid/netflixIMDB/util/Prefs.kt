package com.droid.netflixIMDB.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.R

object Prefs {

    private var sharedPreferences: SharedPreferences? = null

    private const val SKIP_COUNT = "skip_count"
    private const val IS_PREMIUM_USER = "is_premium_user"
    private const val IS_PREMIUM_HINT_SHOWN = "is_premium_hint_shown"
    private const val HAS_SHOWN_YOUTUBE_HINT = "has_shown_youtube_hint"
    private const val LANGUAGE_SELECTED = "language_selected"
    private const val SHOW_LANGUAGE_SELECTION = "show_language_selection"

    private fun getSharedPrefs(): SharedPreferences? {
        sharedPreferences = Application.instance?.getSharedPreferences(
            Application.instance?.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )
        return sharedPreferences
    }

    fun setIsPremiumUser(isPremium: Boolean) {
        getSharedPrefs()?.run {
            edit().putBoolean(IS_PREMIUM_USER, isPremium).apply()
        }
    }

    fun getLanguageSelected(): String {
        return getSharedPrefs()?.run {
            getString(LANGUAGE_SELECTED, "us")
        } ?: "us"
    }

    fun setLanguageSelected(languageCode: String) {
        getSharedPrefs()?.run {
            edit().putString(LANGUAGE_SELECTED, languageCode).apply()
        }
    }

    fun setIsPremiumHintShown(isHintShown: Boolean) {
        getSharedPrefs()?.run {
            edit().putBoolean(IS_PREMIUM_HINT_SHOWN, isHintShown).apply()
        }
    }

    fun setHasShownYoutubeHint(hasShownHint: Boolean) {
        getSharedPrefs()?.run {
            edit().putBoolean(HAS_SHOWN_YOUTUBE_HINT, hasShownHint).apply()
        }
    }

    fun shouldShowLanguageSelection(): Boolean {
        return getSharedPrefs()?.getBoolean(SHOW_LANGUAGE_SELECTION, true) ?: true
    }

    fun setHasSelectedLanguageScreen() {
        getSharedPrefs()?.run {
            edit().putBoolean(SHOW_LANGUAGE_SELECTION, false).apply()
        }
    }


    fun getIsPremiumUser(): Boolean {
        return getSharedPrefs()?.getBoolean(IS_PREMIUM_USER, false) ?: false
    }

    fun hasShownYoutubeHint(): Boolean {
        return getSharedPrefs()?.getBoolean(HAS_SHOWN_YOUTUBE_HINT, false) ?: false
    }

    fun getIsPremiumHintShown(): Boolean {
        return getSharedPrefs()?.getBoolean(IS_PREMIUM_HINT_SHOWN, false) ?: false
    }

    fun getSkipCount(): Int {
        val sharedPrefs = getSharedPrefs()
        return sharedPrefs?.getInt(SKIP_COUNT, 0) ?: 0
    }

    fun incrementSkipCount() {
        val sharedPrefs = getSharedPrefs()
        val currentSkipCount = getSkipCount() + 1
        sharedPrefs?.run {
            this.edit {
                this.putInt(SKIP_COUNT, currentSkipCount)
                this.commit()
            }
        }
    }

    fun hasExceedLimit(): Boolean {
        if (getIsPremiumUser()) {
            return false
        }

        return getSkipCount() >= ReaderConstants.MAX_LIMIT
    }
}
