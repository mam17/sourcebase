package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.libads.admobs.InterstitialAdHelper
import com.example.myapplication.libads.interfaces.InterAdsCallback
import com.example.myapplication.ui.language.LanguageActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun provideViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    private val TAG = "TAG_SPLASH"
    private val interAds by lazy {
        InterstitialAdHelper(
            this,
            BuildConfig.inter_splash
        )
    }

    override fun initViews() {
        super.initViews()
        interAds.load(object : InterAdsCallback {

            override fun onLoadSuccess() {
                interAds.show(this@SplashActivity, this)
                Log.i(TAG, "onLoadSuccess: ")
            }

            override fun onLoadFailed(error: String) {
                gotoMainScreen()
                Log.i(TAG, "onLoadFailed: ")
            }

            override fun onShowSuccess() {
                Log.i(TAG, "onShowSuccess: ")
            }

            override fun onShowFailed(error: String) {
                Log.i(TAG, "onShowFailed: ")
                gotoMainScreen()
            }

            override fun onAdClosed() {
                Log.i(TAG, "onAdClosed: ")
                gotoMainScreen()
            }
        })
    }

    private fun gotoMainScreen() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            LanguageActivity.start(this@SplashActivity, true)
            finish()
        }

    }
}