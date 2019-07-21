package com.droid.netflixIMDB.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.droid.netflixIMDB.Application


object LaunchUtils {


    fun sendFeedbackIntent(context: Context) {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "parallelstudiosinc@gmail.com", null
            )
        )
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")
        context.startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
    }

    fun openPlayStore(context: Context) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${Application.instance?.packageName}")
                )
            )
        } catch (exception: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${Application.instance?.packageName}")
                )
            )
        }

    }

    fun getPlaystoreIntent(): Intent {
        return try {
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${Application.instance?.packageName}"))
        } catch (exception: Exception) {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${Application.instance?.packageName}")
            )
        }
    }

    fun openPrivacyPolicy(context: Context) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://sites.google.com/view/netfliximdbplugin/privacy-policy")
            )
        )
    }

    fun openDontKillMyApp(context: Context) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://dontkillmyapp.com")
            )
        )
    }

    fun openPowerSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            context.startActivity(intent)
        }
    }
}