package com.example.myapplication.utils.ads

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.example.myapplication.utils.ads.admods.AppOpenAds
import com.example.myapplication.utils.ads.admods.BannerAds
import com.example.myapplication.utils.ads.admods.NativeAds
import com.example.myapplication.utils.ads.admods.RewardedAds
import com.example.myapplication.utils.ads.interfaces.AdLoadCallback
import com.example.myapplication.utils.ads.interfaces.AdShowCallback
import com.google.android.gms.ads.nativead.NativeAd

object AdsManager {

    private val interstitialAds = InterstitialAds()
    private val rewardedAds = RewardedAds()
    private val appOpenAds = AppOpenAds()
    private val nativeAds = NativeAds()

    // Interstitial
    fun loadInterstitial(context: Context, adUnitId: String? = null, callback: AdLoadCallback? = null) {
        interstitialAds.loadAd(context, adUnitId, callback)
    }

    fun showInterstitial(activity: Activity, callback: AdShowCallback? = null) {
        interstitialAds.showAd(activity, null, callback)
    }

    fun isInterstitialLoaded(): Boolean {
        return interstitialAds.isLoaded()
    }

    // Rewarded
    fun loadRewarded(context: Context, adUnitId: String? = null, callback: AdLoadCallback? = null) {
        rewardedAds.loadAd(context, adUnitId, callback)
    }

    fun showRewarded(activity: Activity, callback: AdShowCallback? = null) {
        rewardedAds.showAd(activity, null, callback)
    }

    fun isRewardedLoaded(): Boolean {
        return rewardedAds.isLoaded()
    }

    // App Open
    fun loadAppOpen(context: Context, adUnitId: String? = null, callback: AdLoadCallback? = null) {
        appOpenAds.loadAd(context, adUnitId, callback)
    }

    fun showAppOpen(activity: Activity, callback: AdShowCallback? = null) {
        appOpenAds.showAd(activity, null, callback)
    }

    fun isAppOpenLoaded(): Boolean {
        return appOpenAds.isLoaded()
    }

    // Native
    fun loadNative(context: Context, adUnitId: String? = null, callback: AdLoadCallback? = null) {
        nativeAds.loadAd(context, adUnitId, callback)
    }

    fun getNativeAd(): NativeAd? {
        return if (nativeAds.isLoaded()) nativeAds.adObject else null
    }

    private val bannerAds = BannerAds()

    fun loadBanner(context: Context, adUnitId: String? = null, callback: AdLoadCallback? = null) {
        bannerAds.loadAd(context, adUnitId, callback)
    }

    fun showBanner(activity: Activity, container: FrameLayout, callback: AdShowCallback? = null) {
        bannerAds.showAd(activity, container, callback)
    }

    fun isBannerLoaded(): Boolean {
        return bannerAds.isLoaded()
    }

    fun destroyBanner() {
        bannerAds.destroy()
    }


    // Lifecycle
    fun destroyAll() {
        interstitialAds.destroy()
        rewardedAds.destroy()
        appOpenAds.destroy()
        nativeAds.destroy()
    }
}