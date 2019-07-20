package com.droid.netflixIMDB.util

import android.content.Context
import android.content.Intent
import android.net.Uri


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

    fun openPlayStore(context: Context, packageName: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (exception: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
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
}