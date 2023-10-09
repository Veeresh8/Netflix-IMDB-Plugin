package com.droid.netflixIMDB

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Dashboard : AppCompatActivity() {

    companion object {

        fun launch(context: Context) {
            context.startActivity(Intent(context, Dashboard::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
    }
}