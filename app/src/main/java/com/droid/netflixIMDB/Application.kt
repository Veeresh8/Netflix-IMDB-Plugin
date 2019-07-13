package com.droid.netflixIMDB

import android.app.Application

class Application: Application() {

    companion object {
        var instance: Application? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}