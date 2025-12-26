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
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.example.myapplication.libads.admobs.AppOpenAdHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.utils.AppEx.getDeviceLanguage
import com.example.myapplication.utils.AppEx.setAppLanguage
import com.example.myapplication.utils.LocaleHelper
import com.example.myapplication.utils.SpManager
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.MobileAds
import com.tiktok.TikTokBusinessSdk
import com.tiktok.appevents.base.EventName
import com.tiktok.appevents.base.TTBaseEvent
import com.tiktok.appevents.contents.TTContentsEventConstants
import com.tiktok.appevents.contents.TTPurchaseEvent
import dagger.hilt.android.HiltAndroidApp
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import javax.inject.Inject
import javax.inject.Singleton


@HiltAndroidApp
@Singleton
class App : Application(),  Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    @Inject
    lateinit var spManager: SpManager
    lateinit var appOpenAdHelper: AppOpenAdHelper
    private var currentActivity: Activity? = null
    var adsInitialized = false

    @Volatile
    var isOtherFullscreenAdShowing = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }

    override fun onCreate() {
        super<Application>.onCreate()
        context = applicationContext

        initAppsflyer()
        initTiktokSDK()
        initAppOpenAds()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)
    }
    fun initMobileAds() {
        if (adsInitialized) return
        MobileAds.initialize(this)
        adsInitialized = true
    }
    fun initAppOpenAds() {
        appOpenAdHelper = AppOpenAdHelper(
            context = this,
            adUnitId = BuildConfig.appopen_resume,
            adPlacement = AdPlacement.APP_OPEN
        )
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

    private fun initAppsflyer() {
        val appsflyerDevKey = ""
        AppsFlyerLib.getInstance().init(appsflyerDevKey, null, this)
        AppsFlyerLib.getInstance().setDebugLog(BuildConfig.DEBUG)
        AppsFlyerLib.getInstance().start(this, appsflyerDevKey, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d("Appsflyer", "Launch sent successfully, got 200 response code from server");
            }

            override fun onError(i: Int, s: String) {
                Log.d(
                    "Appsflyer", "Launch failed to be sent:\n" +
                            "Error code: " + i + "\n"
                            + "Error description: " + s
                );
            }
        })
    }

    private fun logRevForAppsflyer(valueMicroStr: String?, networkAdapter: String?, currencyCode: String?, adType: String?) {
        runCatching {
            val valueMicro = (valueMicroStr?.toDoubleOrNull() ?: 0.0) / 1_000_000

            if (valueMicro == 0.0)
                return

            val afAdRevData = AFAdRevenueData(
                monetizationNetwork = networkAdapter ?: "",
                mediationNetwork = MediationNetwork.GOOGLE_ADMOB,
                currencyIso4217Code = currencyCode ?: "USD",
                revenue = valueMicro
            )
            AppsFlyerLib.getInstance().logAdRevenue(
                afAdRevData, mapOf(
                    AdRevenueScheme.AD_TYPE to adType,
                )
            )
        }
    }

    private fun pushRevAdmobForFacebook(valueMicros: Double) {
        val value = valueMicros / 1_000_000.0

        // Log purchase event
        AppEventsLogger
            .newLogger(this)
            .logPurchase(BigDecimal.valueOf(value), Currency.getInstance("USD"))

        // Log ad impression event
        val bundle = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "USD")
        }

        AppEventsLogger
            .newLogger(this)
            .logEvent(AppEventsConstants.EVENT_NAME_AD_IMPRESSION, value, bundle)
    }

    private fun initTiktokSDK() {

        val tiktokAppId = ""

        val ttConfig = if (BuildConfig.DEBUG) {
            TikTokBusinessSdk.TTConfig(applicationContext)
                .setAppId(applicationContext.packageName)
                .setTTAppId(tiktokAppId).openDebugMode()
                .setLogLevel(TikTokBusinessSdk.LogLevel.DEBUG)
                .openDebugMode()
                .enableAutoIapTrack()
        } else {
            TikTokBusinessSdk.TTConfig(applicationContext)
                .setAppId(applicationContext.packageName)
                .setTTAppId(tiktokAppId)
                .enableAutoIapTrack()
        }
        TikTokBusinessSdk.initializeSdk(ttConfig, object : TikTokBusinessSdk.TTInitCallback {
            override fun success() {}
            override fun fail(code: Int, msg: String?) {}
        })

        TikTokBusinessSdk.startTrack()
    }

    private fun reportRevForTiktok(currencyCode: String, mValueMicros: String, id: String, adType: String) {
        try {
            if (currencyCode.isEmpty())
                return

            if (mValueMicros.isEmpty())
                return

            if (adType.isEmpty())
                return

            val tiktokRevenueRateForAdsImpressionEvent = 1000000

            val valueMicros = BigDecimal(mValueMicros)

            val revenueRateAdsImpression = tiktokRevenueRateForAdsImpressionEvent
            val valueAdImpression =
                valueMicros.divide(BigDecimal(revenueRateAdsImpression), 3, RoundingMode.HALF_UP)

            val adInfo = TTBaseEvent.newBuilder(EventName.IN_APP_AD_IMPR.toString())
                .addProperty("currency", currencyCode)
                .addProperty("value", valueAdImpression)
                .addProperty("Tiktok_RevenueRateForAdsImpressionEvent", revenueRateAdsImpression)
                .addProperty("content_id", id)
                .addProperty("content_type", adType)
                .build()
            TikTokBusinessSdk.trackTTEvent(adInfo)

            val purchaseInfo = TTPurchaseEvent.newBuilder("Purchase")
                .setContentType(adType)
                .setContentId(id)
                .setCurrency(TTContentsEventConstants.Currency.USD)
                .setValue(valueAdImpression.toDouble())
                .addProperty("Tiktok_RevenueRateForPurchaseEvent", revenueRateAdsImpression)
                .build()

            TikTokBusinessSdk.trackTTEvent(purchaseInfo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i("TAG_APP", "onStart: $currentActivity")
        if (appOpenAdHelper.isReady()) {
            currentActivity?.let { appOpenAdHelper.showIfAvailable(it) }
        }
    }
}