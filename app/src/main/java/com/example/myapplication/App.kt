package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplication.libads.consent.GoogleMobileAdsConsentManager
import com.example.myapplication.utils.AppEx.setAppLanguage
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.LocaleHelper
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ads.AppOpenAdsUtil
import com.example.myapplication.libads.base.BaseAds.Companion.md5
import com.example.myapplication.libads.event.MMPManager
import com.google.android.ump.FormError
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    @Inject
    lateinit var spManager: SpManager
    private var currentActivity: Activity? = null

    private lateinit var openResumeAds: AppOpenAdsUtil
    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        @SuppressLint("StaticFieldLeak")
        private var mInstance: App? = null

        val instance: App?
            get() = mInstance

        var isInterstitialShowing = false
        var isRewardedShowing = false
        var isAppOpenShowing = false

        fun isAnyAdShowing(): Boolean {
            return isInterstitialShowing || isRewardedShowing || isAppOpenShowing
        }
    }

    override fun onCreate() {
        super<Application>.onCreate()
        context = applicationContext
        mInstance = this

        // Thêm lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)

        MMPManager.initialize(
            application = this,
            appToken = "2fm9gkqubvpc", // Token test
            isDebug = BuildConfig.DEBUG
        )

        // Thêm global parameters
        MMPManager.addGlobalCallbackParameter("app_version", BuildConfig.VERSION_NAME)
    }

    @SuppressLint("HardwareIds")
    fun initConsentManager(
        activity: Activity,
        testDeviceIds: List<String> = emptyList(),
        onConsentComplete: () -> Unit
    ) {
        val consentManager = GoogleMobileAdsConsentManager.getInstance(this)
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val hashedId = md5(androidId).uppercase(Locale.getDefault())
        Log.i("TAG_Consent", "hashedId: $hashedId")
        val ids = testDeviceIds.ifEmpty { listOf("hashedId") }

        consentManager.gatherConsent(activity, ids, object :
            GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener {
            override fun onConsentGatheringComplete(formError: FormError?) {
                if (formError != null) {
                    Log.w("TAG_Consent", "Consent form error: ${formError.message}")
                } else {
                    Log.d("TAG_Consent", "Consent complete. Can request ads: ${consentManager.canRequestAds()}")
                    val granted = consentManager.canRequestAds()
                    spManager.putBoolean(Constant.KEY_SP_UMP_SHOWED, granted)
                }
                onConsentComplete()
            }
        })
    }

    fun loadAdsOpenResume(){
        currentActivity?.let { activity ->
            openResumeAds = AppOpenAdsUtil(
                idAds = BuildConfig.appopen_resume,
                idAds2 = null,
                adPlacement = "open_resume",
                isEnable = true,
                checkOtherAdsShowing = { isAnyAdShowing() }
            )
            openResumeAds.load(activity)
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
        if (::openResumeAds.isInitialized && !isAnyAdShowing()) {
            currentActivity?.let { openResumeAds.showIfAvailable(it) }
        }
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
    }
}