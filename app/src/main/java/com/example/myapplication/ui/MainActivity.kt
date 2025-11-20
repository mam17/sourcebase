package com.example.myapplication.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.NotificationUtil
import com.example.myapplication.utils.ads.adsutils.InterstitialAdsUtil
import com.example.myapplication.utils.ads.adsutils.NativeAdsUtil
import com.example.myapplication.utils.ads.adsutils.RewardedAdsUtil
import com.example.myapplication.utils.ads.interfaces.OnAdmobLoadListener
import com.example.myapplication.utils.ads.interfaces.OnAdmobShowListener

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun provideViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    private val rewardedAd by lazy {
        RewardedAdsUtil(
            context = this,
            idAds = BuildConfig.reward_create,
            adPlacement = "reward_main",
            isEnable = true
        )
    }
    private val interSplash by lazy {
        InterstitialAdsUtil(
            context = this,
            idAds = BuildConfig.inter_splash,
            idAds2f = BuildConfig.inter_splash,
            adPlacement = "inter_splash",
            isEnable = true
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtil.createNotificationChannel(this)

        App.instance?.loadAdsOpenResume()

        NativeAdsUtil.loadNativeHome()
        showNativeHome()


        viewBinding.btnInterSplash.setOnClickListener {
            showInterAdsSplash()
        }


        viewBinding.btnReward.setOnClickListener {
            showRewardAds()
        }
    }

    fun loadRewardMain() {
        rewardedAd.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                Log.d("tag_ADS", "Rewarded loaded")
            }

            override fun onError(e: String) {
                Log.d("tag_ADS", "Rewarded load error: $e")
            }
        })
    }

    fun loadInterMain() {
        interSplash.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                Log.d("tag_ADS", "Splash inter loaded")
            }

            override fun onError(e: String) {
                Log.d("tag_ADS", "Splash inter load error: $e")
            }
        })
    }

    fun showRewardAds() {
        if (rewardedAd.isLoaded()) {
            rewardedAd.show(this, object : OnAdmobShowListener {
                override fun onShow() {
                    Log.d("tag_ADS", "Rewarded showed")
                }

                override fun onError(e: String) {
                    Log.d("tag_ADS", "Show rewarded error: $e")
                }
            })
        } else {
            Log.d("tag_ADS", "Rewarded ad not loaded yet")
        }
    }

    fun showInterAdsSplash() {
        interSplash.show(this, object : OnAdmobShowListener {
            override fun onShow() {
                Log.d("tag_ADS", "splash inter showed")
                LanguageActivity.start(this@MainActivity, false)
            }

            override fun onError(e: String) {
                Log.d("tag_ADS", "Show error splash: $e")
            }
        })
    }

    private fun showNativeHome() {
        NativeAdsUtil.homeNativeAdmob?.run {
            getNativeAdLive().observe(this@MainActivity) {
                if (available()) {
                    this.showNative(viewBinding.flAdplaceholder, null)
                }
            }
        }
    }
}
