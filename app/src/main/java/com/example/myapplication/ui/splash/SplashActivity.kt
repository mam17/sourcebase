package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.helper.UmpHelper
import com.example.myapplication.libads.interfaces.UmpCallback
import com.example.myapplication.libads.utils.SplashInterAdsLoader
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.ui.language.LanguageActivity
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun provideViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        initUmpAndAds()
    }

    private fun initUmpAndAds() {
        UmpHelper(this).requestConsent(object : UmpCallback {

            override fun onConsentDone(canRequestAds: Boolean) {
                if (canRequestAds) {
                    initAds()
                } else {
                    gotoMainScreen()
                }
            }
        })
    }
    private fun initAds() {
        val app = application as App
        app.initMobileAds()
        app.appOpenAdHelper.enableShow()
        app.appOpenAdHelper.load()

        SplashInterAdsLoader(
            activity = this@SplashActivity,
            interSplash2f = BuildConfig.inter_splash_2f to AdPlacement.INTER_SPLASH_2F,
            interSplash = BuildConfig.inter_splash to AdPlacement.INTER_SPLASH,
        ) {
            gotoMainScreen()
        }.start()

    }

    private fun gotoMainScreen() {
        MainActivity.start(this)
    }
}
