package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplication.libads.consent.GoogleMobileAdsConsentManager
import com.example.myapplication.utils.AppEx.setAppLanguage
import com.example.myapplication.utils.LocaleHelper
import com.example.myapplication.utils.SpManager
import com.google.android.ump.FormError
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    @Inject
    lateinit var spManager: SpManager
    private var currentActivity: Activity? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        @SuppressLint("StaticFieldLeak")
        private var mInstance: App? = null

        val instance: App?
            get() = mInstance
    }

    override fun onCreate() {
        super<Application>.onCreate() // Sửa: bỏ <Application>
        context = applicationContext
        mInstance = this

        // Thêm lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)
    }

    fun initConsentManager(activity: Activity) {
        // Đảm bảo spManager đã được inject
        if (!::spManager.isInitialized) {
            Log.e("Consent", "spManager not initialized")
            return
        }

        val consentManager = GoogleMobileAdsConsentManager.getInstance(this)

        if (!spManager.getBoolean("consent_requested", false)
            || consentManager.isPrivacyOptionsRequired()
        ) {
            consentManager.gatherConsent(
                activity,
                testDeviceIds = listOf("TEST_DEVICE_HASHED_ID"),
                onComplete = object : GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener {
                    override fun onConsentGatheringComplete(formError: FormError?) {
                        if (formError != null) {
                            Log.w("Consent", "Form error: ${formError.message}")
                        }
                        spManager.putBoolean("consent_requested", true)

                        Log.d(
                            "Consent",
                            "Consent complete. Can request ads: ${consentManager.canRequestAds()}"
                        )
                    }
                }
            )
        } else {
            Log.d("Consent", "Consent already gathered before.")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        try {
            val language = spManager.getLanguage()
            LocaleHelper.onAttach(this, language.languageCode)
        } catch (e: Exception) {
            Log.e("App", "Error in onConfigurationChanged", e)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        try {
            val languageCode = newBase?.let { SpManager.getInstance(it).getLanguage().languageCode }
            val context = languageCode?.let { newBase.setAppLanguage(it) } ?: newBase
            super.attachBaseContext(context)
        } catch (e: Exception) {
            super.attachBaseContext(newBase)
            Log.e("App", "Error in attachBaseContext", e)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i("TAG_APP", "onStart: $currentActivity")
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d("TAG_APP", "onActivityCreated: ${activity::class.java.simpleName}")
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        Log.d("TAG_APP", "onActivityStarted: ${activity::class.java.simpleName}")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("TAG_APP", "onActivityResumed: ${activity::class.java.simpleName}")
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d("TAG_APP", "onActivityPaused: ${activity::class.java.simpleName}")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("TAG_APP", "onActivityStopped: ${activity::class.java.simpleName}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d("TAG_APP", "onActivitySaveInstanceState: ${activity::class.java.simpleName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("TAG_APP", "onActivityDestroyed: ${activity::class.java.simpleName}")
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}