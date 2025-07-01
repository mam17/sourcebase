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
        val locale = getDeviceLanguage()
        val language = spManager.getLanguage()
        LocaleHelper.onAttach(this, language.languageCode)
        super.onConfigurationChanged(newConfig)
    }

    override fun attachBaseContext(newBase: Context?) {
        val languageCode = newBase?.let { SpManager.getInstance(it).getLanguage().languageCode }
        val context = languageCode?.let { newBase.setAppLanguage(it) }
        super.attachBaseContext(context)
    }
}