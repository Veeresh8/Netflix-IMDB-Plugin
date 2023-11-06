package com.droid.netflixIMDB

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.blongho.country_data.World
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.mixpanel.android.mpmetrics.MixpanelAPI


class Application : Application() {

    companion object {
        var instance: Application? = null

        val mixpanel: MixpanelAPI by lazy {
            MixpanelAPI.getInstance(instance, "941835f539a966bc725a76c273d32e12", true)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        World.init(applicationContext)

        AppCenter.start(
            this, "4cf6d42d-64c9-4f64-a3c4-46826ffe3679",
            Analytics::class.java, Crashes::class.java
        )
    }
}