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
import com.example.myapplication.libads.utils.AdsGlobalState
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppOpenAdHelper(
    context: Context,
    private val adUnitId: String,
    private val adUnitIdFloor: String? = null,
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
    private var canShow = true

    /* ================= CONTROL ================= */

    fun enableShow() { canShow = true }
    fun disableShow() { canShow = false }

    /* ================= LOAD ================= */

    fun load(targetAdUnitId: String = adUnitId, callback: InterAdsCallback? = null) {
        if (isLoading || appOpenAd != null) return

        isLoading = true
        resetTracker()

        AppOpenAd.load(
            context,
            targetAdUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.i(TAG, "AppOpen loaded: $targetAdUnitId")
                    appOpenAd = ad
                    isLoading = false

                    ad.setOnPaidEventListener { adValue ->
                        onPaid(adValue, ad.responseInfo)
                    }
                    callback?.onLoadSuccess()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i(TAG, "Load failed: $targetAdUnitId - ${error.message}")
                    isLoading = false
                    appOpenAd = null
                    callback?.onLoadFailed(error.message)
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
                Log.i(TAG, "AppOpen Floor failed, switching to Normal ID")
                load(adUnitId, callback)
            }

            override fun onShowSuccess() {}
            override fun onShowFailed(error: String) {}
            override fun onAdClosed() {}
        })
    }

    /* ================= SHOW ================= */

    fun showIfAvailable(activity: Activity, loadingTime: Long = 2000L) {
        if (!canShow) {
            Log.i(TAG, "Show disabled")
            return
        }

        val app = activity.application as App
        if (app.isOtherFullscreenAdShowing) {
            Log.i(TAG, "Inter/Reward showing → skip AppOpen")
            return
        }

        if (isShowing) {
            Log.i(TAG, "AppOpen is already showing")
            return
        }

        val ad = appOpenAd ?: run {
            loadWithFloor()
            return
        }

        val dialog = DialogAdsLoading(activity)
        dialog.show()

        (activity as? LifecycleOwner)?.lifecycleScope?.launch {
            delay(loadingTime)

            if (activity.isFinishing || activity.isDestroyed) {
                if (dialog.isShowing) dialog.dismiss()
                return@launch
            }

            dialog.dismiss()

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    isShowing = true
                    AdsGlobalState.isFullscreenAdShowing = true
                    logImpression(mediation = getMediationName(ad.responseInfo))
                }

                override fun onAdDismissedFullScreenContent() {
                    cleanup()
                    loadWithFloor()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(TAG, "Show failed: ${error.message}")
                    cleanup()
                    loadWithFloor()
                }
            }

            ad.show(activity)
        }
    }

    /* ================= UTILS ================= */

    private fun cleanup() {
        isShowing = false
        AdsGlobalState.isFullscreenAdShowing = false
        appOpenAd = null
    }

    fun isReady(): Boolean = appOpenAd != null
}
