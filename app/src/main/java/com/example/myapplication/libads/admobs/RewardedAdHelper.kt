package com.example.myapplication.libads.admobs

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.App
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.helper.DialogAdsLoading
import com.example.myapplication.libads.interfaces.InterAdsCallback // Bạn có thể dùng chung interface này hoặc tạo RewardedAdsCallback tương tự
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsGlobalState
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RewardedAdHelper(
    context: Context,
    private val adUnitId: String,
    private val adUnitIdFloor: String? = null, // Thêm ID Floor
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

    // Hàm load core nhận ID cụ thể
    fun load(targetAdUnitId: String = adUnitId, callback: InterAdsCallback? = null) {
        if (isLoading || rewardedAd != null) return
        isLoading = true
        resetTracker()

        RewardedAd.load(
            context,
            targetAdUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    ad.setOnPaidEventListener { adValue ->
                        onPaid(adValue, ad.responseInfo)
                    }
                    callback?.onLoadSuccess()
                    Log.i(TAG, "Loaded: $targetAdUnitId")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    callback?.onLoadFailed(error.message)
                    Log.e(TAG, "Failed: $targetAdUnitId - Error: ${error.message}")
                }
            }
        )
    }

    // Hàm load logic 2 bước (Ưu tiên Floor)
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
                Log.i(TAG, "Reward Floor failed, switching to Normal ID")
                load(adUnitId, callback)
            }

            override fun onShowSuccess() {}
            override fun onShowFailed(error: String) {}
            override fun onAdClosed() {}
        })
    }

    /* ================= SHOW ================= */

    fun showRewarded(
        activity: Activity,
        onActionNext: () -> Unit
    ) {
        if (!isReady()) {
            android.widget.Toast.makeText(activity, "The advertisement is not ready, please try again later.", android.widget.Toast.LENGTH_SHORT).show()
            if (!isLoading) loadWithFloor()
            return
        }

        show(
            activity = activity,
            onUserEarnedReward = {
                Log.i(TAG, "showRewarded: ")
            },
            onClosed = { onActionNext() },
            onFailed = {
                android.widget.Toast.makeText(activity, "Ad display failed", android.widget.Toast.LENGTH_SHORT).show()
                onActionNext()
            }
        )
    }
    fun show(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onClosed: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null,
        loadingTime: Long = 1500L,
        isReload: Boolean = true
    ) {
        if (isShowing) {
            onFailed?.invoke("Reward is showing")
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            onFailed?.invoke("Reward not ready")
            if (isReload) loadWithFloor()
            return
        }

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
                    isShowing = true
                    (activity.application as App).isOtherFullscreenAdShowing = true
                    logImpression(mediation = getMediationName(ad.responseInfo))
                }

                override fun onAdDismissedFullScreenContent() {
                    (activity.application as App).isOtherFullscreenAdShowing = false
                    cleanup()
                    onClosed?.invoke()
                    if (isReload) loadWithFloor()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    (activity.application as App).isOtherFullscreenAdShowing = false
                    cleanup()
                    onFailed?.invoke(error.message)
                    if (isReload) loadWithFloor()
                }
            }

            ad.show(activity) {
                onUserEarnedReward()
            }
        }
    }

    /* ================= UTILS ================= */

    fun isReady(): Boolean = rewardedAd != null

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
