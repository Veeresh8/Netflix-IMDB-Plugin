package com.droid.netflixIMDB.analytics

import android.os.Bundle
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.util.ReaderConstants

object Analytics {

    fun postPurchasePayload(purchaseType: String) {
        val bundle = Bundle()
        bundle.putString(ReaderConstants.PURCHASE_TYPE, purchaseType)
        Application.firebaseAnalytics?.logEvent(ReaderConstants.PURCHASE, bundle)
    }

    fun postClickEvents(clickTypes: ClickTypes) {
        val bundle = Bundle()
        bundle.putString(ReaderConstants.CLICK_TYPE, clickTypes.name)
        Application.firebaseAnalytics?.logEvent(ReaderConstants.CLICK, bundle)
    }

    enum class ClickTypes(name: String) {
        PLAYSTORE("playstore"),
        FEEDBACK("feedback"),
        PRIVACY_POLICY("privacy_policy"),
        CUSTOMIZE("customize"),
        SUPPORT("support"),
        FAQ("faq"),
        SETTINGS("settings"),

        OVERLAY("overlay_permission"),
        ACC_SERV("acc_serv"),
        WHITELIST("whitelist"),

        SMALL_PURCHASE("purchase_small")
    }
}