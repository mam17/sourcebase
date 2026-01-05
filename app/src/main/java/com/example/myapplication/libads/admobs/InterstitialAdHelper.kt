package com.example.myapplication.libads.admobs

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.App
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.helper.DialogAdsLoading
import com.example.myapplication.libads.interfaces.InterAdsCallback
import com.example.myapplication.libads.utils.AdPlacement
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InterstitialAdHelper(
    context: Context,
    private val adUnitId: String,
    private val adUnitIdFloor: String? = null,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = context,
    adPlacement = adPlacement,
    adType = "interstitial"
) {

    companion object {
        private const val TAG = "InterstitialAdHelper"
    }

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isShowing = false

    /* ================= LOAD ================= */
    fun load(targetAdUnitId: String = adUnitId, callback: InterAdsCallback? = null) {
        if (isLoading || interstitialAd != null) return
        isLoading = true

        InterstitialAd.load(
            context,
            targetAdUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    resetTracker()
                    interstitialAd = ad
                    isLoading = false

                    interstitialAd?.setOnPaidEventListener { adValue ->
                        onPaid(adValue, interstitialAd?.responseInfo)
                    }

                    callback?.onLoadSuccess()
                    Log.i(TAG, "Loaded: $targetAdUnitId")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    callback?.onLoadFailed(error.message)
                    Log.e(TAG, "Failed: $targetAdUnitId - Error: ${error.message}")
                }
            }
        )
    }

    fun loadWithFloor(callback: InterAdsCallback? = null) {
        if (adUnitIdFloor == null) {
            load(adUnitId, callback)
            return
        }

        load(adUnitIdFloor, object : InterAdsCallback {
            override fun onLoadSuccess() {
                callback?.onLoadSuccess()
            }

            override fun onLoadFailed(error: String) {
                Log.i(TAG, "Floor failed, switching to Normal ID")
                load(adUnitId, callback)
            }

            override fun onShowSuccess() {}
            override fun onShowFailed(error: String) {}
            override fun onAdClosed() {}
        })
    }

    /* ================= SHOW ================= */
    fun showInterstitial(
        activity: Activity, loadingTime: Long = 2000L, onActionNext: () -> Unit
    ) {
        show(activity, object : InterAdsCallback {
            override fun onAdClosed() { onActionNext() }
            override fun onShowFailed(error: String) { onActionNext() }
            override fun onLoadSuccess() {}
            override fun onLoadFailed(error: String) {}
            override fun onShowSuccess() {}
        }, loadingTime = loadingTime)
    }

    fun show(
        activity: Activity,
        callback: InterAdsCallback? = null,
        loadingTime: Long = 2000L,
        isReload: Boolean = true
    ) {
        if (isShowing) {
            callback?.onShowFailed("Interstitial is showing")
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            callback?.onShowFailed("Interstitial not ready")
            if (isReload) loadWithFloor(callback)
            return
        }

        isShowing = true

        val dialog = DialogAdsLoading(activity)
        dialog.show()
        (activity as? LifecycleOwner)?.lifecycleScope?.launch {
            delay(loadingTime)

            if (activity.isFinishing || activity.isDestroyed) {
                if (dialog.isShowing) dialog.dismiss()
                isShowing = false
                return@launch
            }

            dialog.dismiss()

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    (activity.application as App).isOtherFullscreenAdShowing = true
                    logImpression(mediation = getMediationName(ad.responseInfo))
                    callback?.onShowSuccess()
                }

                override fun onAdDismissedFullScreenContent() {
                    (activity.application as App).isOtherFullscreenAdShowing = false
                    cleanup()
                    callback?.onAdClosed()
                    if (isReload) loadWithFloor()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    (activity.application as App).isOtherFullscreenAdShowing = false
                    cleanup()
                    callback?.onShowFailed(error.message)
                    if (isReload) loadWithFloor()
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

}
