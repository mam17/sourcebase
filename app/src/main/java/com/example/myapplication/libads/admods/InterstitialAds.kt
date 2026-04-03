package com.example.myapplication.libads.admods

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.myapplication.libads.base.BaseAds
import com.example.myapplication.libads.event.MMPManager.logAdRevenue
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.math.BigDecimal
import java.util.Currency

class InterstitialAds(
    context: Context,
    private val adUnitId: String,
    private val adPlacement: String = "",
    private val reloadOnClose: Boolean = false,
) : BaseAds(context.applicationContext) {

    companion object {
        private const val TAG = "TAG_InterstitialAds"
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isShowing = false

    /* ================= TIMEOUT ================= */

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    /* ================= LOAD ================= */

    fun load(callback: OnAdmobLoadListener, timeoutMillis: Long = 30_000L) {
        if (isPro()) {
            callback.onLoad() // Still call onLoad to allow app logic to continue
            return
        }
        if (isLoading || interstitialAd != null) {
            Log.i(TAG, "load() ignored (loading or already loaded) - $adPlacement")
            return
        }

        Log.i(TAG, "load() - Placement: $adPlacement")
        isLoading = true
        onAdmobLoadListener = callback

        timeoutRunnable = Runnable {
            if (interstitialAd == null) {
                Log.i(TAG, "load timeout - $adPlacement")
                isLoading = false
                onAdmobLoadListener?.onError("timeout")
                onAdmobLoadListener = null
            }
        }
        timeoutHandler.postDelayed(timeoutRunnable!!, timeoutMillis)

        InterstitialAd.load(
            context,
            adUnitId,
            adRequestBuilder.build(),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    clearTimeout()
                    Log.i(TAG, "onAdLoaded - $adPlacement")

                    interstitialAd = ad
                    isLoading = false

                    setupPaidEvent(ad)

                    onAdmobLoadListener?.onLoad()
                    onAdmobLoadListener = null
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    clearTimeout()
                    Log.i(TAG, "onAdFailedToLoad: ${error.message} - $adPlacement")

                    interstitialAd = null
                    isLoading = false

                    onAdmobLoadListener?.onError(error.message)
                    onAdmobLoadListener = null
                }
            }
        )
    }

    /* ================= SHOW ================= */

    fun show(activity: Activity, listener: OnAdmobShowListener) {
        if (isPro()) {
            listener.onShow()
            listener.onClosed()
            return
        }
        val ad = interstitialAd
        if (ad == null || isShowing || activity.isFinishing || activity.isDestroyed) {
            listener.onError("inter not available")
            return
        }

        isShowing = true

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "onAdShowed - $adPlacement")
                listener.onShow()
            }

            override fun onAdDismissedFullScreenContent() {
                Log.i(TAG, "onAdDismissed - $adPlacement")
                cleanupAfterShow()
                listener.onClosed()

                if (reloadOnClose) reload()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.i(TAG, "onAdFailedToShow: ${error.message}")
                cleanupAfterShow()
                listener.onError(error.message)
                listener.onClosed()
            }
        }

        ad.show(activity)
    }

    /* ================= HELPERS ================= */

    private fun reload() {
        if (interstitialAd != null || isLoading) return
        Log.i(TAG, "reload interstitial - $adPlacement")

        load(object : OnAdmobLoadListener {
            override fun onLoad() {
                Log.i(TAG, "reload success - $adPlacement")
            }

            override fun onError(e: String) {
                Log.i(TAG, "reload failed: $e")
            }
        })
    }

    private fun cleanupAfterShow() {
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        isShowing = false
    }

    private fun clearTimeout() {
        timeoutRunnable?.let { timeoutHandler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    private fun setupPaidEvent(ad: InterstitialAd) {
        ad.setOnPaidEventListener { adValue ->
            val revenue = adValue.valueMicros / 1_000_000.0
            if (revenue > 0) {
                AppEventsLogger.newLogger(context).logPurchase(
                    BigDecimal.valueOf(revenue),
                    Currency.getInstance("USD")
                )
            }

            context.logAdRevenue(
                adValue = adValue,
                adUnitId = adPlacement,
                responseInfo = ad.responseInfo,
                adType = "ad_interstitial"
            )
        }
    }

    fun available(): Boolean {
        val available = interstitialAd != null && !isShowing
        Log.i(TAG, "available=$available - $adPlacement")
        return available
    }

    fun destroy() {
        clearTimeout()
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
        isLoading = false
        isShowing = false
    }
}