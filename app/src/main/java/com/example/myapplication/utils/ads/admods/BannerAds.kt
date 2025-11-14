package com.example.myapplication.utils.ads.admods

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.myapplication.utils.ads.base.BaseAds
import com.example.myapplication.utils.ads.interfaces.AdLoadCallback
import com.example.myapplication.utils.ads.interfaces.AdShowCallback
import com.google.android.gms.ads.*
import java.util.Date

class BannerAds : BaseAds<AdView>() {

    companion object {
        private const val TAG = "TAG_BannerAds"
    }

    private var adView: AdView? = null

    override fun loadAd(
        context: Context,
        idAdUnit: String?,
        callback: AdLoadCallback?
    ) {
        if (isLoadingAd) {
            Log.d(TAG, "Banner is already loading.")
            return
        }

        isLoadingAd = true
        val unitId = idAdUnit ?: TEST_BANNER
        val adRequest = AdRequest.Builder().build()

        // Tạo AdView mới mỗi lần load để tránh lỗi “val cannot be reassigned”
        adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = unitId // chỉ gán 1 lần ở đây
        }

        // Gắn listener sau khi tạo AdView
        adView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "✅ Banner loaded successfully.")
                adObject = adView
                loadTime = Date()
                isLoadingAd = false
                callback?.onAdLoaded()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "❌ Failed to load banner: ${error.message}")
                destroy()
                isLoadingAd = false
                callback?.onAdFailed(error)
            }
        }

        adView?.loadAd(adRequest)
    }

    override fun showAd(
        activity: Activity,
        container: FrameLayout?,
        callback: AdShowCallback?
    ) {
        val banner = adObject ?: run {
            Log.w(TAG, "⚠️ Banner not loaded yet.")
            callback?.onAdFailedToShow()
            return
        }

        (banner.parent as? ViewGroup)?.removeView(banner)
        container?.removeAllViews()
        container?.addView(banner)

        callback?.onAdShown()
        Log.d(TAG, "📢 Banner shown in container.")
    }

    override fun destroy() {
        super.destroy()
        adView?.destroy()
        adView = null
    }
}

