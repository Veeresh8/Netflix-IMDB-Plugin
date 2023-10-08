package com.droid.netflixIMDB

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.blongho.country_data.World
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.Locale


class Application : Application() {

    private lateinit var locale: Locale

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
        setLocale()
    }

    private fun setLocale() {
        val resources = resources
        locale = Locale("us")
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}