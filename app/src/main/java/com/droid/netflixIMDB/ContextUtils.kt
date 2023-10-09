package com.droid.netflixIMDB

import android.content.Context
import android.content.res.Configuration
import com.droid.netflixIMDB.util.Prefs
import java.util.Locale


object ContextUtils {

    fun setAppLocale(context: Context, language: String) {
        val languageToChange = shouldModifyLanguageCode(language)

        val locale = Locale(languageToChange)
        Locale.setDefault(locale)

        val resources = context.resources

        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        Prefs.setLanguageSelected(languageToChange)
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