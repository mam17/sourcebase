package com.example.myapplication.ui

import android.app.Activity
import android.content.Intent
import com.example.myapplication.BuildConfig
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.libads.admobs.InterstitialAdHelper
import com.example.myapplication.libads.admobs.RewardedAdHelper
import com.example.myapplication.libads.interfaces.InterAdsCallback
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.ui.language.LanguageActivity

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

    private lateinit var interstitialAd: InterstitialAdHelper
    private lateinit var rewardedAd: RewardedAdHelper
    override fun initViews() {
        super.initViews()
        setupAdsMain()
        setupListener()
    }

    private fun setupAdsMain() {
        interstitialAd = InterstitialAdHelper(
            context = this,
            adUnitId = BuildConfig.inter_main,
            adPlacement = AdPlacement.INTER_MAIN
        )

        rewardedAd = RewardedAdHelper(
            context = this,
            adUnitId = BuildConfig.reward_create,
            adPlacement = AdPlacement.REWARD_MAIN
        )

        interstitialAd.load(object : InterAdsCallback {
            override fun onLoadSuccess() {}
            override fun onLoadFailed(error: String) {}
            override fun onShowSuccess() {}
            override fun onShowFailed(error: String) {}
            override fun onAdClosed() {}
        })
        rewardedAd.load()
    }

    private fun setupListener() {
        viewBinding.apply {
            btnShowInter.setOnClickListener {
                interstitialAd.show(
                    activity = this@MainActivity,
                    callback = object : InterAdsCallback {
                        override fun onShowSuccess() {}
                        override fun onShowFailed(error: String) {
                            LanguageActivity.start(this@MainActivity)
                        }

                        override fun onAdClosed() {
                            LanguageActivity.start(this@MainActivity)
                        }

                        override fun onLoadSuccess() {}
                        override fun onLoadFailed(error: String) {
                            LanguageActivity.start(this@MainActivity)
                        }
                    }
                )
            }
            btnShowReward.setOnClickListener {
                if (rewardedAd.isReady()) {
                    rewardedAd.show(
                        activity = this@MainActivity,
                        onReward = { },
                        onClosed = { LanguageActivity.start(this@MainActivity) },
                        onFailed = { error ->
                            showToast(error)
                        }
                    )
                } else {
                    showToast("Reward Ads loading failed")
                }
            }
        }
    }


}