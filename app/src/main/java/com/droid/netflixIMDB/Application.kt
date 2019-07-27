package com.droid.netflixIMDB

import android.app.Application
import com.droid.netflixIMDB.util.Prefs
import com.google.firebase.analytics.FirebaseAnalytics

class Application : Application() {

    companion object {
        var instance: Application? = null
        var firebaseAnalytics: FirebaseAnalytics? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        Prefs.initPackages()
    }
}