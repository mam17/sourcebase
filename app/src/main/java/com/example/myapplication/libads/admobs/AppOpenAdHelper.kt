package com.example.myapplication.libads.admobs

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsGlobalState
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdHelper(
    context: Context,
    private val adUnitId: String,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = context,
    adPlacement = adPlacement,
    adType = "app_open"
) {

    companion object {
        private const val TAG = "AppOpenAdHelper"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoading = false
    private var isShowing = false
    private var canShow = true   // 👈 enable / disable

    /* ================= CONTROL ================= */

    fun enableShow() {
        canShow = true
    }

    fun disableShow() {
        canShow = false
    }

    /* ================= LOAD ================= */

    fun load() {
        if (isLoading || appOpenAd != null) return

        isLoading = true
        resetTracker()

        AppOpenAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),

            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.i(TAG, "AppOpen loaded")
                    appOpenAd = ad
                    isLoading = false

                    ad.setOnPaidEventListener { adValue ->
                        onPaid(adValue, ad.responseInfo)
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i(TAG, "Load failed: ${error.message}")
                    isLoading = false
                    appOpenAd = null
                }
            }
        )
    }

    /* ================= SHOW ================= */

    fun showIfAvailable(activity: Activity) {
        val app = activity.application as App
        if (!canShow) {
            Log.i(TAG, "Show disabled")
            return
        }

        if (app.isOtherFullscreenAdShowing) {
            Log.i(TAG, "Inter/Reward showing → skip AppOpen")
            return
        }

        val ad = appOpenAd ?: return

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {
                    isShowing = true
                    AdsGlobalState.isFullscreenAdShowing = true
                    logImpression(
                        mediation = getMediationName(ad.responseInfo)
                    )
                }

                override fun onAdDismissedFullScreenContent() {
                    cleanup()
                    load()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Show failed: ${error.message}")
                    cleanup()
                    load()
                }
            }

        ad.show(activity)
    }

    /* ================= UTILS ================= */

    private fun cleanup() {
        isShowing = false
        AdsGlobalState.isFullscreenAdShowing = false
        appOpenAd = null
    }

    fun isReady(): Boolean = appOpenAd != null
}
