package com.example.myapplication.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun onAttach(context: Context?, defaultLanguage: String): Context? {
        return setLocale(context, defaultLanguage)
    }

    fun setLocale(context: Context?, language: String): Context? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else {
            updateResourcesLegacy(context, language)
        }
    }

    private fun updateResources(context: Context?, language: String): Context? {
        val locale = Locale.forLanguageTag(language.lowercase(Locale.ROOT))
        Locale.setDefault(locale)

        val configuration = context?.resources?.configuration?.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }

        return configuration?.let { context.createConfigurationContext(it) }
    }

    private fun updateResourcesLegacy(context: Context?, language: String): Context? {
        val locale = Locale.forLanguageTag(language.lowercase(Locale.ROOT))
        Locale.setDefault(locale)

        val resources = context?.resources
        resources?.let {
            val configuration = it.configuration
            configuration.locale = locale
            configuration.setLayoutDirection(locale)
            it.updateConfiguration(configuration, it.displayMetrics)
        }

        return context
    }
}
