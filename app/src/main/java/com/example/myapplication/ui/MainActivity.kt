package com.example.myapplication.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import com.example.myapplication.BuildConfig
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.libads.admobs.BannerAdHelper
import com.example.myapplication.libads.admobs.InterstitialAdHelper
import com.example.myapplication.libads.admobs.NativeAdHelper
import com.example.myapplication.libads.admobs.RewardedAdHelper
import com.example.myapplication.libads.interfaces.BannerShimmerController
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.BannerGravity
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
    private lateinit var bannerAd: BannerAdHelper
    private lateinit var nativeAdHelper: NativeAdHelper

    override fun initViews() {
        super.initViews()
        setupAdsMain()
        setupListener()
    }

    private fun setupAdsMain() {
        bannerAd = BannerAdHelper(
            activity = this,
            adUnitId = BuildConfig.banner_splash,
            adUnitIdFloor = BuildConfig.banner_splash,
            adPlacement = AdPlacement.BANNER_MAIN
        )

        bannerAd.loadBanner(
            parent = viewBinding.adBanner.shimmerBanner,
            container = viewBinding.adBanner.shimmerBanner,
            isCollapsible = true,
            gravity = BannerGravity.BOTTOM
        )

        nativeAdHelper = NativeAdHelper(
            activity = this,
            adUnitId = BuildConfig.native_home,
            adUnitIdFloor = BuildConfig.native_home,
            adPlacement = AdPlacement.NATIVE_MAIN
        )
        nativeAdHelper.loadNativeAd(
            parent = viewBinding.flNativeContainer.root,
            shimmer = viewBinding.shimmerNative)

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

        interstitialAd.loadWithFloor()
        rewardedAd.loadWithFloor()

    }

    private fun setupListener() {
        viewBinding.apply {
            btnShowInter.setOnClickListener {
                interstitialAd.showInterstitial(
                    activity = this@MainActivity,
                    onActionNext = { LanguageActivity.start(this@MainActivity) }
                )
            }
            btnShowReward.setOnClickListener {
                rewardedAd.showRewarded(
                    this@MainActivity,
                    onActionNext = { LanguageActivity.start(this@MainActivity) })

            }
        }
    }


}