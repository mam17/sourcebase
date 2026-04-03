package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.libads.admods.InterstitialAds
import com.example.myapplication.libads.admods.NativeAds
import com.afproject.iap.IapFactory
import com.afproject.iap.listener.BillingClientConnectionListener
import com.example.myapplication.libads.consent.GoogleMobileAdsConsentManager
import com.example.myapplication.libads.helper.CollapsiblePositionType
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsEx
import com.example.myapplication.libads.utils.AdsEx.getInterstitialId
import com.example.myapplication.libads.utils.BannerAdsUntil.initBanner
import com.example.myapplication.libads.utils.NativeAdsUtil
import com.example.myapplication.libads.utils.NativeAdsUtil.loadNativeFullSplash
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.AppEx.observeOnce
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val TAG = "TAG_SplashActivity"
    private var adsInterSplash: InterstitialAds? = null
    private var billingSplashDispatched = false

    private val billingReadyListener = object : BillingClientConnectionListener {
        override fun onConnected(status: Boolean, billingResponseCode: Int) {
            dispatchBillingSplashReady()
        }
    }

    override fun provideViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        setFullScreen()

        val iap = IapFactory.getInstance()
        if (iap.isBillingClientReady()) {
            dispatchBillingSplashReady()
        } else {
            iap.registerBillingClientConnectionListener(billingReadyListener)
            viewBinding.root.postDelayed({ dispatchBillingSplashReady() }, 2_500L)
        }

        viewBinding.layoutNativeFSTimer.btnCloseOnb.setOnClickListener {
            gotoMainScreen()
        }
    }

    private fun dispatchBillingSplashReady() {
        if (billingSplashDispatched) return
        billingSplashDispatched = true
        IapFactory.getInstance().unregisterBillingClientConnectionListener(billingReadyListener)
        runOnUiThread { checkAndLoadAds() }
    }

    override fun onDestroy() {
        IapFactory.getInstance().unregisterBillingClientConnectionListener(billingReadyListener)
        super.onDestroy()
    }

    private fun checkAndLoadAds() {
        if (isPro) {
            delayGotoMain()
            return
        }

        if (GoogleMobileAdsConsentManager.getInstance(this).canRequestAds()) {
            App.instance?.initSDKs()
            initBannerAds()
            loadNativeFullSplash(isCheckOpenApp)
            loadInterSplash()
        } else {
            App.instance?.initConsentManager(this) {
                App.instance?.initSDKs()
                initBannerAds()
                loadNativeFullSplash(isCheckOpenApp)
                loadInterSplash()
            }
        }
    }

    private fun initBannerAds() {
        initBanner(
            activity = this,
            shimmer = viewBinding.adViewContainer.shimmerBanner,
            primaryAdUnitId = AdsEx.getBannerId(BuildConfig.banner_splash),
            secondaryAdUnitId = AdsEx.getBannerId(BuildConfig.banner_splash),
            adPlacement = AdPlacement.BANNER_SPLASH,
            collapsiblePosition = CollapsiblePositionType.NONE
        )
    }


    private fun loadInterSplash() {
        if (remoteConfig.inter_splash.enabled) {
            adsInterSplash =
                InterstitialAds(
                    this,
                    getInterstitialId(remoteConfig.inter_splash.id),
                    AdPlacement.INTER_SPLASH
                )
            adsInterSplash?.load(object : OnAdmobLoadListener {
                override fun onLoad() {
                    showInterAdsSplash()
                }

                override fun onError(e: String) {
                    adsInterSplash = InterstitialAds(
                        this@SplashActivity,
                        BuildConfig.inter_splash,
                        AdPlacement.INTER_SPLASH,
                        false
                    )
                    adsInterSplash?.load(object : OnAdmobLoadListener {
                        override fun onLoad() {
                            showInterAdsSplash()
                        }

                        override fun onError(e: String) {
                            showNativeSplash()
                        }
                    })
                }
            })
        } else {
            showNativeSplash()
        }
    }

    private fun showInterAdsSplash() {
        if (remoteConfig.inter_splash.enabled) {
            adsInterSplash?.show(this, object : OnAdmobShowListener {
                override fun onShow() {
                }

                override fun onError(e: String) {
                    showNativeSplash()
                }

                override fun onClosed() {
                    showNativeSplash()
                }
            })
        } else {
            showNativeSplash()
        }
    }

    private fun showNativeSplash() {
        if (!remoteConfig.native_fs_splash.enabled) {
            return delayGotoMain()
        }

        val ad = NativeAdsUtil.splashNativeFullAdmob
        if (ad != null && ad.available()) {
            performShowNativeSplash(ad)
        } else {
            loadNativeFullSplash(isCheckOpenApp) { loadedAd ->
                performShowNativeSplash(loadedAd)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (NativeAdsUtil.splashNativeFullAdmob == null || !NativeAdsUtil.splashNativeFullAdmob!!.available()) {
                    delayGotoMain()
                }
            }, 5000)
        }
    }

    private fun performShowNativeSplash(ad: NativeAds) {
        ad.getNativeAdLive().observeOnce(this@SplashActivity) {
            if (ad.available()) {
                viewBinding.layoutNativeFSTimer.shimmerNativeFullScreen.visible()
                ad.showNative(
                    viewBinding.layoutNativeFSTimer.shimmerNativeFullScreen,
                    R.id.native_ad_view,
                    null
                )
                startNativeCountdown()
            } else {
                delayGotoMain()
            }
        }
    }

    private fun startNativeCountdown() {
        viewBinding.layoutNativeFSTimer.rlCloseAds.visible()
        object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                if (seconds < 1) {
                    viewBinding.layoutNativeFSTimer.tvTimeCount.gone()
                    viewBinding.layoutNativeFSTimer.btnCloseOnb.visible()
                    cancel()
                } else {
                    viewBinding.layoutNativeFSTimer.tvTimeCount.text = seconds.toString()
                }
            }

            override fun onFinish() {
                viewBinding.layoutNativeFSTimer.tvTimeCount.gone()
                viewBinding.layoutNativeFSTimer.btnCloseOnb.visible()
            }
        }.start()
    }

    private fun delayGotoMain() {
        Log.i(TAG, "delayGotoMain: ")
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500)
            gotoMainScreen()
        }
    }


    private fun gotoMainScreen() {
        if (isCheckOpenApp){
            LanguageActivity.start(this, true)
            finish()
        }else{
            MainActivity.start(this)
            finish()
        }

    }

}