package com.example.myapplication.libads.admobs

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import com.example.myapplication.libads.adsbase.BaseAdTracker
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsGlobalState
import com.example.myapplication.libads.events.MMPManager.logAdRevenue
import com.example.myapplication.libads.events.MMPManager.logInterstitialRevenue
import com.example.myapplication.libads.helper.DialogAdsLoading
import com.example.myapplication.libads.interfaces.InterAdsCallback
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Currency

class InterstitialAdHelper(
    private val context: Context,
    private val adUnitId: String,
    private val adPlacement: AdPlacement
): BaseAdTracker() {

    companion object {
        private const val TAG = "InterstitialAdHelper"
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isShowing = false

    /* ================= LOAD ================= */

    fun load(callback: InterAdsCallback) {
        if (isLoading || interstitialAd != null) {
            Log.i(TAG, "load ignored")
            return
        }

        isLoading = true

        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.i(TAG, "onAdLoaded")
                    resetTracker()
                    interstitialAd = ad
                    isLoading = false

                    interstitialAd?.setOnPaidEventListener { adValue ->
                        logPaidOnce {
                            context.logInterstitialRevenue(
                                adValue = adValue,
                                placement = adPlacement,
                                responseInfo = interstitialAd?.responseInfo
                            )
                        }
                    }

                    callback.onLoadSuccess()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i(TAG, "onAdFailedToLoad: ${error.message}")
                    interstitialAd = null
                    isLoading = false
                    callback.onLoadFailed(error.message)
                }
            }
        )
    }

    /* ================= SHOW ================= */

    fun show(
        activity: Activity,
        callback: InterAdsCallback,
        loadingTime: Long = 2000L
    ) {
        if (isShowing) {
            callback.onShowFailed("Interstitial is showing")
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            callback.onShowFailed("Interstitial not ready")
            return
        }

        val dialog = DialogAdsLoading(activity)
        dialog.show()

        CoroutineScope(Dispatchers.Main).launch {
            delay(loadingTime)

            if (activity.isFinishing || activity.isDestroyed) {
                dialog.dismiss()
                return@launch
            }

            dialog.dismiss()

            ad.fullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdShowedFullScreenContent() {
                        AdsGlobalState.isFullscreenAdShowing = true
                        logImpressionOnce {
                            FirebaseAnalytics.getInstance(context).logEvent(
                                "ad_impression",
                                bundleOf(
                                    "ad_platform" to "admob",
                                    "ad_unit_name" to adPlacement.value,
                                    "ad_format" to "interstitial",
                                    "mediation" to getMediationName()
                                )
                            )
                        }
                        callback.onShowSuccess()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        isShowing = false
                        AdsGlobalState.isFullscreenAdShowing = false
                        interstitialAd = null
                        callback.onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        isShowing = false
                        AdsGlobalState.isFullscreenAdShowing = false
                        interstitialAd = null
                        callback.onShowFailed(error.message)
                    }
                }

            ad.show(activity)
        }
    }

    /* ================= UTILS ================= */

    fun isReady(): Boolean = interstitialAd != null
    fun isShowing(): Boolean = isShowing

    fun destroy() {
        cleanup()
        isLoading = false
    }

    private fun cleanup() {
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        isShowing = false
    }

    private fun getMediationName(): String {
        return interstitialAd
            ?.responseInfo
            ?.loadedAdapterResponseInfo
            ?.adSourceName
            ?: "unknown"
    }
}
