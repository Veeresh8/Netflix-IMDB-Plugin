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
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.droid.netflixIMDB.Application
import com.droid.netflixIMDB.R
import com.droid.netflixIMDB.analytics.Analytics
import com.droid.netflixIMDB.toast


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

    fun shareAppIntent(context: Context) {
        try {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey, check out this app: https://play.google.com/store/apps/details?id=com.droid.netflixIMDB"
            )
            sendIntent.type = "text/plain"
            context.startActivity(sendIntent)
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching share intent - ${exception.message}")
        }
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
        Analytics.postClickEvents(Analytics.ClickTypes.ACC_SERV)
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
        } catch (exception: java.lang.Exception) {
            Log.e(TAG, "Exception launching - ${exception.message}")
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
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://sites.google.com/view/netfliximdbplugin/privacy-policy")
                )
            )
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching - ${exception.message}")
        }
    }

    fun openDontKillMyApp(context: Context) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://dontkillmyapp.com")
                )
            )
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching - ${exception.message}")
        }
    }

    fun openPowerSettings(context: Context) {
        Analytics.postClickEvents(Analytics.ClickTypes.WHITELIST)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent()
                intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                context.startActivity(intent)
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Exception launching - ${exception.message}")
        }
    }
}