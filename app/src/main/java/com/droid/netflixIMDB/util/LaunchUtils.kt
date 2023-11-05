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
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


object LaunchUtils {

    private val TAG: String = this.javaClass.simpleName

    @OptIn(DelicateCoroutinesApi::class)
    fun sendFeedbackIntent(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val advertId = getAdvertisingId()

            withContext(Dispatchers.Main) {
                try {
                    val emailIntent = Intent(Intent.ACTION_SENDTO)
                    emailIntent.data = Uri.parse("mailto:") // This sets the URI for sending emails

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("parallelstudiosinc@gmail.com")) // Replace with the recipient's email address
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Ad Skipper Feedback")
                    emailIntent.putExtra(Intent.EXTRA_TEXT, """
                        Device ID - $advertId
                        
                        
                        
                    """.trimIndent())

                    context.startActivity(emailIntent)

                } catch (exception: Exception) {
                    Log.e(TAG, "Exception launching feedback intent - ${exception.message}")
                }
            }
        }
    }

    fun getAdvertisingId(): String? {
        return try {
            val adInfo = Application.instance?.let { AdvertisingIdClient.getAdvertisingIdInfo(it) }
            adInfo?.id
        } catch (exception: java.lang.Exception) {
            null
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

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
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