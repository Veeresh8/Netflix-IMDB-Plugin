package com.droid.netflixIMDB

import android.content.Context
import android.content.ContextWrapper
import java.util.Locale

class ContextUtils (base: Context) : ContextWrapper(base) {
    companion object {
        fun setAppLocale(context: Context, language: String) {
            val languageToChange = shouldModifyLanguageCode(language)
            val locale = Locale(languageToChange)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.createConfigurationContext(config)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        private fun shouldModifyLanguageCode(language: String): String {
            return when(language) {
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
}