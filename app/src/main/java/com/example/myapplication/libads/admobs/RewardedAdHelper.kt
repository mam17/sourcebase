package com.example.myapplication.libads.admobs

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.helper.DialogAdsLoading
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsGlobalState
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RewardedAdHelper(
    context: Context,
    private val adUnitId: String,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = context,
    adPlacement = adPlacement,
    adType = "reward"
) {

    companion object {
        private const val TAG = "RewardedAdHelper"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var isShowing = false

    /* ================= LOAD ================= */
    fun load() {
        if (isLoading || rewardedAd != null) return

        isLoading = true

        resetTracker()

        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false

                    ad.setOnPaidEventListener { adValue ->
                        onPaid(adValue, ad.responseInfo)
                    }

                    Log.i(TAG, "Reward loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    Log.e(TAG, "Load failed: ${error.message}")
                }
            }
        )
    }

    /* ================= SHOW ================= */
    fun show(
        activity: Activity,
        isReload: Boolean = true,
        onReward: () -> Unit,
        onClosed: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null,
        loadingTime: Long = 1500L
    ) {
        if (isShowing) {
            onFailed?.invoke("Reward is showing")
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            onFailed?.invoke("Reward not ready")
            if (isReload) load()
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
                        isShowing = true
                        (activity.application as App).isOtherFullscreenAdShowing = true
                        logImpression(
                            mediation = getMediationName(ad.responseInfo)
                        )
                    }

                    override fun onAdDismissedFullScreenContent() {
                        (activity.application as App)
                            .isOtherFullscreenAdShowing = false
                        cleanup()
                        onClosed?.invoke()
                        if (isReload) load()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        (activity.application as App)
                            .isOtherFullscreenAdShowing = false
                        cleanup()
                        onFailed?.invoke(error.message)
                        if (isReload) load()
                    }
                }

            ad.show(activity) {
                onReward()
            }
        }
    }

    fun isReady(): Boolean = rewardedAd != null
    fun isShowing(): Boolean = isShowing

    fun destroy() {
        cleanup()
        isLoading = false
    }

    private fun cleanup() {
        rewardedAd?.fullScreenContentCallback = null
        rewardedAd = null
        isShowing = false
        AdsGlobalState.isFullscreenAdShowing = false
    }
}
