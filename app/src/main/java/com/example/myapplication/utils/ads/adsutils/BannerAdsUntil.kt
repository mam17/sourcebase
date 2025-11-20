package com.example.myapplication.utils.ads.adsutils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.utils.ads.admods.BannerAdLoader
import com.example.myapplication.utils.ads.admods.BannerAds
import com.example.myapplication.utils.ads.helper.CollapsiblePositionType
import com.facebook.shimmer.ShimmerFrameLayout
import java.util.Timer
import kotlin.concurrent.timer

object BannerAdsUntil {
    private const val TAG = "TAG_BannerAdsUntil"
    private var bannerAds: BannerAds? = null
    private var bannerAdAdaptive: BannerAdLoader? = null
    private val handler = Handler(Looper.getMainLooper())
    private var bannerTimer: Timer? = null

    private var lastInterstitialTime = 0L
    private var lastAppOpenTime = 0L
    private val interstitialCooldown = 5 * 60 * 1000L

    // ------------------- Banner -------------------
    fun initBannerAdaptive(activity: Activity, adUnit: String, shimmer: ShimmerFrameLayout) {
        bannerAdAdaptive = BannerAdLoader(activity, adUnit, shimmer)
        Log.i(TAG, "BannerAd initialized")
    }

    fun loadBanner() {
        bannerAdAdaptive?.loadBanner()
    }

    fun destroyBanner() {
        stopBannerAutoReload()
        bannerAdAdaptive?.destroy()
    }

    private fun startBannerAutoReload(interval: Long = 30_000L) {
        stopBannerAutoReload()
        bannerTimer = timer(period = interval) {
            handler.post {
                bannerAdAdaptive?.loadBanner()
            }
        }
    }

    private fun stopBannerAutoReload() {
        bannerTimer?.cancel()
        bannerTimer = null
    }

    fun initBanner(
        activity: Activity,
        primaryAdUnitId: String,
        secondaryAdUnitId: String? = null,
        shimmer: ShimmerFrameLayout,
        collapsiblePosition: CollapsiblePositionType = CollapsiblePositionType.NONE
    ) {
        bannerAds = BannerAds(activity, collapsiblePosition)

        if (!secondaryAdUnitId.isNullOrEmpty()) {
            Log.i(TAG, "Trying to load secondary/floor ad: $secondaryAdUnitId")
            bannerAds?.showBannerWithFallback(
                activity = activity,
                primaryAdUnitId = primaryAdUnitId,
                secondaryAdUnitId = secondaryAdUnitId,
                parent = shimmer
            )
        } else {
            Log.i(TAG, "Loading primary ad only: $primaryAdUnitId")
            bannerAds?.showBanner(activity, primaryAdUnitId, shimmer)
        }
    }


    fun reloadBanner() {
        bannerAds?.reload()
    }

    fun onResume() {
        bannerAds?.onResume()
    }

    fun onPause() {
        bannerAds?.onPause()
    }

    fun onDestroy() {
        bannerAds?.onDestroy()
        bannerAds = null
    }
}