package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.libads.consent.GoogleMobileAdsConsentManager
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.libads.utils.BannerAdsUntil.initBanner
import com.example.myapplication.libads.helper.CollapsiblePositionType
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val TAG = "TAG_SplashActivity"

    override fun provideViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        if (GoogleMobileAdsConsentManager.getInstance(this).canRequestAds()) {
            initBannerAds()
            proceedToMainScreen()
        } else {
            App.instance?.initConsentManager(this) {
                initBannerAds()
                proceedToMainScreen()
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
            collapsiblePosition = CollapsiblePositionType.BOTTOM
        )
    }


    private fun proceedToMainScreen() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            gotoMainScreen()
        }
    }

    private fun gotoMainScreen() {
        MainActivity.start(this)
        finish()
    }

}