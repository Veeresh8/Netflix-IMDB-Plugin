package com.droid.netflixIMDB.util

import android.app.Activity
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.droid.netflixIMDB.Application


object LaunchUtils {

    private val TAG: String = this.javaClass.simpleName

    fun sendFeedbackIntent(context: Context) {
        try {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "parallelstudiosinc@gmail.com", null
                )
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "")
            context.startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching feedback intent - ${exception.message}")
        }
    }

    fun openIgnoreBatteryOptimisations(context: Context) {
        if (!isIgnoringBatteryOptimizations(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent()
                val packageName: String = context.packageName
                val pm = context.getSystemService(POWER_SERVICE) as PowerManager?
                if (!pm?.isIgnoringBatteryOptimizations(packageName)!!) {
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    context.startActivity(intent)
                }
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }

    fun launchAppWithPackageName(activity: Activity, packageName: String) {
        val pm = activity.packageManager
        try {
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            activity.startActivity(launchIntent)
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching - ${exception.message}")
            Toast.makeText(activity, "App not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchAccessibilityScreen(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        } catch (exception: java.lang.Exception) {
            Log.e(TAG, "Exception launching - ${exception.message}")
        }
    }
}