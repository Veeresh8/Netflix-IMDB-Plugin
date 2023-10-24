package com.droid.netflixIMDB

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.droid.netflixIMDB.util.Prefs
import java.util.Locale


object ContextUtils {

    fun setAppLocale(context: Context, language: String): Context {
        val languageToChange = shouldModifyLanguageCode(language)

        Prefs.setLanguageSelected(languageToChange)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else {
            updateResourcesLegacy(context, language)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    private fun shouldModifyLanguageCode(language: String): String {
        return when (language) {
            "se" -> {
                "sv"
            }

            "ae" -> {
                "ar"
            }

            "cn" -> {
                "zh"
            }

            "gr" -> {
                "el"
            }

            "in" -> {
                "hi"
            }

            else -> {
                language
                }
            }
        }
}