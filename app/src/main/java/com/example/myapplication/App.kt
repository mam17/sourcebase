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
import com.applovin.sdk.AppLovinPrivacySettings
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.bytedance.sdk.openadsdk.api.PAGConstant
import com.example.myapplication.libads.base.BaseAds.Companion.md5
import com.example.myapplication.libads.consent.GoogleMobileAdsConsentManager
import com.example.myapplication.libads.firebase.FirebaseConfigManager
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsEx
import com.example.myapplication.libads.utils.AppOpenAdsUtil
import com.afproject.iap.IapFactory
import com.afproject.iap.IapItem
import com.afproject.iap.IapType
import com.example.myapplication.billing.IapAppIntegration
import com.example.myapplication.utils.AppEx.setAppLanguage
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.LocaleHelper
import com.example.myapplication.utils.SpManager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.ads.mediation.inmobi.InMobiConsent
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.ResponseInfo
import com.google.android.ump.FormError
import com.google.firebase.FirebaseApp
import com.inmobi.sdk.InMobiSdk
import com.ironsource.mediationsdk.IronSource
import com.mbridge.msdk.MBridgeConstans
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.tiktok.TikTokBusinessSdk
import com.tiktok.appevents.base.EventName
import com.tiktok.appevents.base.TTBaseEvent
import com.tiktok.appevents.contents.TTContentsEventConstants
import com.tiktok.appevents.contents.TTPurchaseEvent
import com.vungle.ads.VunglePrivacySettings
import dagger.hilt.android.HiltAndroidApp
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    @Inject
    lateinit var spManager: SpManager
    private var currentActivity: Activity? = null
    private var isSDKInitialized = false

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

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(this)

        FirebaseApp.initializeApp(this)
        FirebaseConfigManager.instance().fetch()

        IapFactory.initialize(
            this,
            listOf(
                IapItem(Constant.PRODUCT_ID_YEAR, IapType.SUBSCRIPTION),
                IapItem(Constant.PRODUCT_ID_WEEK, IapType.SUBSCRIPTION),
                IapItem(Constant.PRODUCT_ID_REMOVE_ADS, IapType.PURCHASE),
            ),
            debugMode = false,
        )
        IapAppIntegration.registerProSync(this)
    }

    fun initSDKs() {
        if (isSDKInitialized) return
        isSDKInitialized = true

        Log.i("TAG_APP", "initSDKs: Start initializing SDKs")
        MobileAds.initialize(this)

        initTiktokSDK()
        initAppsflyer()
        FacebookSdk.setAutoLogAppEventsEnabled(true)
        
        initMediation(true)
    }

    private fun initMediation(isGranted: Boolean) {
        initPangle(isGranted)
        initVungle(isGranted)
        initApplovin(isGranted)
        initFAN(isGranted)
        initMintegral(isGranted)
        initInMobi(isGranted)
        initIronSource(isGranted)
    }

    private fun initFAN(isGranted: Boolean) {
        com.facebook.ads.AudienceNetworkAds.initialize(this)
    }

    private fun initVungle(isGranted: Boolean) {
        VunglePrivacySettings.setGDPRStatus(isGranted, "v1.0.0")
    }

    private fun initPangle(isGranted: Boolean) {
        // Pangle handles it via Mediation Adapter usually
    }

    private fun initApplovin(isGranted: Boolean) {
        AppLovinPrivacySettings.setDoNotSell(!isGranted, this)
        VunglePrivacySettings.setCCPAStatus(isGranted)
    }

    private fun initInMobi(isGranted: Boolean) {
        val consentObject = JSONObject()
        try {
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, isGranted)
            consentObject.put("gdpr", if (isGranted) "1" else "0")
        } catch (exception: JSONException) {
            exception.printStackTrace()
        }

        InMobiConsent.updateGDPRConsent(consentObject)
    }

    private fun initMintegral(isGranted: Boolean) {
        val sdk = MBridgeSDKFactory.getMBridgeSDK()
        sdk.setConsentStatus(this, if (isGranted) MBridgeConstans.IS_SWITCH_ON else MBridgeConstans.IS_SWITCH_OFF)
    }

    private fun initIronSource(isGranted: Boolean) {
        IronSource.setConsent(isGranted)
        IronSource.setMetaData("do_not_sell", if (isGranted) "false" else "true")
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
        val ids = testDeviceIds.ifEmpty { listOf(hashedId) }

        consentManager.gatherConsent(activity, ids, object :
            GoogleMobileAdsConsentManager.OnConsentGatheringCompleteListener {
            override fun onConsentGatheringComplete(formError: FormError?) {
                if (formError != null) {
                    Log.w("TAG_Consent", "Consent form error: ${formError.message}")
                } else {
                    Log.d(
                        "TAG_Consent",
                        "Consent complete. Can request ads: ${consentManager.canRequestAds()}"
                    )
                    val granted = consentManager.canRequestAds()
                    spManager.putBoolean(Constant.KEY_SP_UMP_SHOWED, granted)
                }
                onConsentComplete()
            }
        })
    }

    fun loadAdsOpenResume() {
        currentActivity?.let { activity ->
            openResumeAds = AppOpenAdsUtil(
                idAds = AdsEx.getAppOpenId(BuildConfig.appopen_resume),
                idAds2 = null,
                adPlacement = AdPlacement.APPOPEN_RESUME,
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

    private fun initAppsflyer() {
        val appsflyerDevKey = "X6Kiaov2ZqhCk3fc7dGnCd"
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

    private fun logRevForAppsflyer(
        valueMicroStr: String?,
        networkAdapter: String?,
        currencyCode: String?,
        adType: String?
    ) {
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

        val tiktokAppId = "7573468271706505234"

        val ttConfig = if (BuildConfig.DEBUG) {
            TikTokBusinessSdk.TTConfig(applicationContext)
                .setAppId(applicationContext.packageName)
                .setTTAppId(tiktokAppId).openDebugMode()
                .setLogLevel(TikTokBusinessSdk.LogLevel.DEBUG)
                .openDebugMode()
        } else {
            TikTokBusinessSdk.TTConfig(applicationContext)
                .setAppId(applicationContext.packageName)
                .setTTAppId(tiktokAppId)
        }
        TikTokBusinessSdk.initializeSdk(ttConfig, object : TikTokBusinessSdk.TTInitCallback {
            override fun success() {}
            override fun fail(code: Int, msg: String?) {}
        })

        TikTokBusinessSdk.startTrack()
    }

    private fun reportRevForTiktok(
        currencyCode: String,
        mValueMicros: String,
        id: String,
        adType: String
    ) {
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

    fun handleAdRevenue(
        adValue: AdValue,
        adUnitId: String = "",
        responseInfo: ResponseInfo? = null,
        adType: String = "unknown"
    ) {
        val revenue = adValue.valueMicros / 1_000_000.0

        if (revenue <= 0) return

        val currencyCode = "USD"
        val networkAdapter = responseInfo?.mediationAdapterClassName ?: "admob"

        pushRevAdmobForFacebook(adValue.valueMicros.toDouble())

        logRevForAppsflyer(
            valueMicroStr = adValue.valueMicros.toString(),
            networkAdapter = networkAdapter,
            currencyCode = currencyCode,
            adType = adType
        )

        reportRevForTiktok(
            currencyCode = currencyCode,
            mValueMicros = adValue.valueMicros.toString(),
            id = adUnitId.ifEmpty { "unknown_${adType}_${System.currentTimeMillis()}" },
            adType = adType
        )
    }

}