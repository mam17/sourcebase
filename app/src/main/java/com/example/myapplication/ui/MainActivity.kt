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
import com.example.myapplication.utils.ads.InterstitialAdsUtil
import com.example.myapplication.utils.ads.NativeAdsUtil
import com.example.myapplication.utils.ads.RewardedAdsUtil

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
            isEnable = true
        )
    }
    private val interSplash by lazy {
        InterstitialAdsUtil(
            context = this,
            idAds = BuildConfig.inter_splash,
            idAds2f = BuildConfig.inter_splash,
            idAdsPlace = "inter_splash",
            isEnable = true
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtil.createNotificationChannel(this)

        App.instance?.loadAdsOpenResume()

        NativeAdsUtil.loadNativeHome()
        showNativeHome()
        loadRewardMain()
        loadInterMain()

        viewBinding.btnInterSplash.setOnClickListener {
            showInterAdsSplash()
        }


        viewBinding.btnReward.setOnClickListener {
            showRewardAds()
        }
    }

    fun loadRewardMain() {
        rewardedAd.load()
    }

    fun loadInterMain() {
        interSplash.load()
    }

    fun showRewardAds() {
        if (rewardedAd.isLoaded()) {
            rewardedAd.show(this) { success ->
                if (success) {
                    Log.i("tag_ADS", "showRewardAds: show reward")
                    LanguageActivity.start(this, false)
                } else {
                    showToast("Load ads RewardedAds failed")
                }
            }
        } else {
            showToast("Load ads RewardedAds failed")
        }
    }

    fun showInterAdsSplash() {
        interSplash.show(this, onDone = {
            Log.i("tag_ADS", "showInterAdsSplash: Done")
            LanguageActivity.start(this)
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
