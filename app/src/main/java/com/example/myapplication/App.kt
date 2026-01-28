package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.example.myapplication.utils.AppEx.getDeviceLanguage
import com.example.myapplication.utils.AppEx.setAppLanguage
import com.example.myapplication.utils.LocaleHelper
import com.example.myapplication.utils.SpManager
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@HiltAndroidApp
@Singleton
class App : Application() {
    @Inject
    lateinit var spManager: SpManager

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        getDeviceLanguage()
        val language = spManager.getLanguage()
        LocaleHelper.onAttach(this, language.languageCode)
        super.onConfigurationChanged(newConfig)
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val prefs = newBase.getSharedPreferences("transparent", MODE_PRIVATE)

            val languageJson = prefs.getString("key_sp_current_language", "")

            val languageCode = if (!languageJson.isNullOrEmpty()) {
                val regex = "\"languageCode\":\"([^\"]+)\"".toRegex()
                regex.find(languageJson)?.groups?.get(1)?.value ?: "en"
            } else {
                "en"
            }

            val updatedContext = newBase.setAppLanguage(languageCode)
            super.attachBaseContext(updatedContext)
        } else {
            super.attachBaseContext(newBase)
        }
    }
}