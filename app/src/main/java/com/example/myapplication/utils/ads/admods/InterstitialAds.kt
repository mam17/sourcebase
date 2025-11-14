package com.example.myapplication.utils.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.example.myapplication.ui.dialog.DialogAdsLoading
import com.example.myapplication.utils.ads.base.BaseAds
import com.example.myapplication.utils.ads.interfaces.AdLoadCallback
import com.example.myapplication.utils.ads.interfaces.AdShowCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Class quản lý Interstitial Ads.
 */
class InterstitialAds : BaseAds<InterstitialAd>() {

    companion object {
        private const val TAG = "TAG_InterAds"
    }

    private var dialogLoading: DialogAdsLoading? = null

    override fun loadAd(
        context: Context,
        adUnitId: String?,
        callback: AdLoadCallback?
    ) {
        if (isLoading()) {
            Log.d(TAG, "Interstitial is already loading.")
            return
        }

        isLoadingAd = true
        val unitId = adUnitId ?: TEST_INTERSTITIAL
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, unitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "✅ Interstitial loaded successfully.")
                adObject = ad
                loadTime = java.util.Date()
                isLoadingAd = false
                callback?.onAdLoaded()
            }

            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                Log.e(TAG, "❌ Failed to load interstitial: ${error.message}")
                adObject = null
                isLoadingAd = false
                callback?.onAdFailed(error)
            }
        })
    }

    override fun showAd(
        activity: Activity,
        container: FrameLayout?,
        callback: AdShowCallback?
    ) {
        val interstitial = adObject
        if (interstitial == null) {
            Log.w(TAG, "⚠️ Interstitial not loaded yet.")
            callback?.onAdFailedToShow()
            return
        }

        // Khởi tạo dialog loading nếu chưa có
        if (dialogLoading == null) {
            dialogLoading = DialogAdsLoading(activity)
        }

        // Hiển thị dialog loading trước
        dialogLoading?.show()

        // Delay 2 giây trước khi show ad
        android.os.Handler(activity.mainLooper).postDelayed({
            interstitial.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial closed.")
                    dialogLoading?.dismiss()
                    adObject = null
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${error.message}")
                    dialogLoading?.dismiss()
                    callback?.onAdFailedToShow()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial is shown.")
                    dialogLoading?.dismiss()
                    callback?.onAdShown()
                }
            }

            interstitial.show(activity)
        }, 2000) // 2000ms = 2 giây
    }


    override fun destroy() {
        super.destroy()
        adObject = null
        dialogLoading = null
    }
}
