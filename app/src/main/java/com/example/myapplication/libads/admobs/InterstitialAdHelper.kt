package com.example.myapplication.libads.admobs

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.libads.interfaces.InterAdsCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialAdHelper(
    private val context: Context,
    private val adUnitId: String
) {

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
                    interstitialAd = ad
                    isLoading = false
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

    fun show(activity: Activity, callback: InterAdsCallback) {
        val ad = interstitialAd

        if (
            ad == null ||
            isShowing ||
            activity.isFinishing ||
            activity.isDestroyed
        ) {
            callback.onShowFailed("Interstitial not ready")
            return
        }

        isShowing = true

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "onAdShowed")
                callback.onShowSuccess()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.i(TAG, "onAdDismissed")
                cleanup()
                callback.onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.i(TAG, "onAdFailedToShow: ${error.message}")
                cleanup()
                callback.onShowFailed(error.message)
                callback.onAdClosed()
            }
        }

        ad.show(activity)
    }

    /* ================= UTILS ================= */

    fun isReady(): Boolean = interstitialAd != null

    fun destroy() {
        cleanup()
        isLoading = false
    }

    private fun cleanup() {
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        isShowing = false
    }
}
