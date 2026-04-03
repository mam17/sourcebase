package com.example.myapplication.libads.admods

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.libads.base.BaseAds
import com.example.myapplication.libads.event.MMPManager.logAdRevenue
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.math.BigDecimal
import java.util.Currency

class RewardedAds(
    context: Context,
    private val id: String,
    private val adPlacement: String = ""
) : BaseAds(context) {

    companion object {
        private const val TAG = "TAG_reward"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var earnReward = false

    init {
        Log.i(TAG, "RewardAdmob: $id")
    }

    fun load(onAdmobLoadListener: OnAdmobLoadListener?) {
        if (isPro()) {
            onAdmobLoadListener?.onLoad()
            return
        }
        if (isLoading || rewardedAd != null) return

        isLoading = true
        Log.i(TAG, "load rewarded")

        RewardedAd.load(
            context,
            id,
            adRequestBuilder.build(),
            object : RewardedAdLoadCallback() {

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    onAdmobLoadListener?.onError(error.message)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    onAdmobLoadListener?.onLoad()

                    ad.setOnPaidEventListener { value ->
                        val revenue = value.valueMicros / 1_000_000.0
                        if (revenue > 0) {
                            AppEventsLogger.newLogger(context)
                                .logPurchase(
                                    BigDecimal.valueOf(revenue),
                                    Currency.getInstance("USD")
                                )
                        }

                        context.logAdRevenue(
                            adValue = value,
                            adUnitId = adPlacement,
                            responseInfo = rewardedAd?.responseInfo,
                            adType = "ad_rewarded"
                        )
                    }
                }
            }
        )
    }

    fun show(activity: Activity, onAdmobShowListener: OnAdmobShowListener) {
        if (isPro()) {
            onAdmobShowListener.onShow()
            onAdmobShowListener.onClosed()
            return
        }
        Log.i(TAG, "show rewarded")
        earnReward = false

        val ad = rewardedAd
        if (ad == null || activity.isFinishing || activity.isDestroyed) {
            onAdmobShowListener.onError("Ad not ready or activity finishing")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // nothing
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                onAdmobShowListener.onError(adError.message)
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                if (earnReward) {
                    onAdmobShowListener.onShow()
                } else {
                    onAdmobShowListener.onError("no reward")
                }
            }
        }

        ad.show(activity) {
            earnReward = true
        }
    }

    fun loaded(): Boolean {
        Log.i(TAG, "loaded: ${rewardedAd != null}")
        return rewardedAd != null
    }
}