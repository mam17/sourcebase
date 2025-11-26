package com.example.myapplication.libads.admods

import android.app.Activity
import android.util.Log
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class BannerAdLoader(
    private val activity: Activity,
    private val idAds: String,
    private val shimmerLayout: ShimmerFrameLayout
) {

    var adView: AdView? = null
    private var currentAdHeightDp = 100
    private var isLoading = false

    init {
        createAdView(currentAdHeightDp)
    }

    private fun createAdView(heightDp: Int) {
        shimmerLayout.removeAllViews()

        adView = AdView(activity).apply {
            setAdSize(getAdaptiveAdSize(heightDp))
            adUnitId = idAds
        }

        shimmerLayout.addView(adView)
    }

    fun loadBanner() {
        if (isLoading) return
        isLoading = true

        shimmerLayout.startShimmer()

        val view = adView ?: return

        view.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("BannerAdLoader", "Ad loaded with height $currentAdHeightDp dp")
                isLoading = false
                shimmerLayout.hideShimmer()
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("BannerAdLoader", "Ad failed: ${error.message}")
                isLoading = false

                if (currentAdHeightDp == 100) {
                    currentAdHeightDp = 50
                    retryWithNewAdSize()
                } else {
                    loadFallbackBanner()
                }
            }
        }

        val adRequest = AdRequest.Builder().build()
        view.loadAd(adRequest)
    }

    private fun getAdaptiveAdSize(heightDp: Int): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val density = displayMetrics.density

        var adWidthPixels = shimmerLayout.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = displayMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getInlineAdaptiveBannerAdSize(adWidth, heightDp)
    }

    private fun retryWithNewAdSize() {
        createAdView(currentAdHeightDp)
        loadBanner()
    }

    private fun loadFallbackBanner() {
        destroy()
        shimmerLayout.hideShimmer()
        Log.e("BannerAdLoader", "Fallback: No banner available.")
    }

    fun destroy() {
        adView?.destroy()
        adView = null
        shimmerLayout.removeAllViews()
    }
}