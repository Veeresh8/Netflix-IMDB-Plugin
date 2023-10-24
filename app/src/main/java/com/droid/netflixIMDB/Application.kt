package com.droid.netflixIMDB

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.blongho.country_data.World
import com.droid.netflixIMDB.util.Prefs
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.Locale


class Application : Application() {

    companion object {
        var instance: Application? = null
        var firebaseAnalytics: FirebaseAnalytics? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        World.init(applicationContext)
    }
}