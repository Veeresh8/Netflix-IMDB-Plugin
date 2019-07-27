package com.droid.netflixIMDB.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.PayloadCount
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.notifications.NotificationManager
import com.google.gson.Gson


object Prefs {

    private var sharedPreferences: SharedPreferences? = null
    private val TAG: String = javaClass.simpleName

    private const val TITLE_COLOR = "title_color"
    private const val BACKGROUND_COLOR = "background_color"
    private const val ICON_COLOR = "icon_color"
    private const val VIEW_TIMEOUT = "view_timeout"
    private const val REQUESTS_MADE = "requests_made"
    private const val PUSH_TOKEN = "push_token"
    private const val PAYLOAD_COUNT = "payload_count"
    private const val IS_PREMIUM_USER = "is_premium_user"
    private const val IS_PREMIUM_HINT_SHOWN = "is_premium_hint_shown"

    private var titlesRequested: Set<String> = HashSet()

    private fun getSharedPrefs(): SharedPreferences? {
        sharedPreferences = Application.instance?.getSharedPreferences(
            Application.instance?.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )
        return sharedPreferences
    }

    fun addTitle(title: String? = "NULL") {
        titlesRequested.plus(title)
    }

    fun getAllTitlesRequested(): Set<String> {
        return titlesRequested
    }

    fun setPushToken(token: String) {
        getSharedPrefs()?.run {
            edit().putString(PUSH_TOKEN, token).apply()
        }
    }

    fun setIsPremiumUser(isPremium: Boolean) {
        getSharedPrefs()?.run {
            edit().putBoolean(IS_PREMIUM_USER, isPremium).apply()
        }
    }

    fun setIsPremiumHintShown(isHintShown: Boolean) {
        getSharedPrefs()?.run {
            edit().putBoolean(IS_PREMIUM_HINT_SHOWN, isHintShown).apply()
        }
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

    fun incrementRequestMade() {
        val requestsMade = getRequestsMade()
        requestsMade?.run {
            requestsMade
            getSharedPrefs()?.run {
                edit().putInt(REQUESTS_MADE, requestsMade + 1).apply()
            }
        }
    }

    fun setIconColor(color: Int) {
        getSharedPrefs()?.run {
            edit().putInt(ICON_COLOR, color).apply()
        }
    }

    private fun getRequestsMade(): Int? {
        return getSharedPrefs()
            ?.getInt(REQUESTS_MADE, 0)
    }

    fun getIsPremiumUser(): Boolean? {
        return getSharedPrefs()
            ?.getBoolean(IS_PREMIUM_USER, false)
    }

    fun getIsPremiumHintShown(): Boolean {
        return getSharedPrefs()?.getBoolean(IS_PREMIUM_HINT_SHOWN, false)!!
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

    fun getPushToken(): String? {
        return getSharedPrefs()
            ?.getString(PUSH_TOKEN, null)
    }

    fun getPayloadCount(): PayloadCount? {
        var payloadCount: PayloadCount? = null

        val sharedPrefs = getSharedPrefs()
        sharedPrefs?.run {
            payloadCount = if (this.getString(PAYLOAD_COUNT, null) == null) {
                PayloadCount()
            } else {
                val payload = this.getString(PAYLOAD_COUNT, null)
                Gson().fromJson(payload, PayloadCount::class.java)
            }
        }

        return payloadCount
    }

    fun savePayloadCount(payloadCount: PayloadCount) {
        val sharedPrefs = getSharedPrefs()
        sharedPrefs?.run {
            val json = Gson().toJson(payloadCount)
            this.edit {
                this.putString(PAYLOAD_COUNT, json)
                this.commit()
            }
        }
    }

    fun getPayloadTotalCount(): Int {
        var count = 0
        val payloadCount = getPayloadCount()
        payloadCount?.run {
            count = this.netflix.plus(prime).plus(hotstar).plus(youtube)
        }
        return count
    }

    fun hasExceedLimit(): Boolean {
        val payloadCount = getPayloadTotalCount()
        val isPremiumUser = getIsPremiumUser()
        isPremiumUser?.run {
            if (!this && payloadCount >= 3) {
                Application.instance?.let {
                    NotificationManager.createLauncherPushNotification(
                        it,
                        "Enjoying ${Application.instance?.getString(R.string.app_name)}?",
                        "We've served over $payloadCount hits. " +
                                "Please go pro to get unlimited hits."
                    )
                }
                return true
            }
        }
        return false
    }
}
