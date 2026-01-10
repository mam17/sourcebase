package com.example.myapplication.libads.admobs

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.BannerGravity
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

class BannerAdHelper(
    private val activity: Activity,
    private val adUnitId: String,
    private val adUnitIdFloor: String? = null,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = activity,
    adPlacement = adPlacement,
    adType = "banner"
) {

    private var adView: AdView? = null
    private var isLoading = false
    private var collapsiblePositionType = BannerGravity.BOTTOM

    companion object {
        private const val TAG = "BannerAdHelper"
    }

    fun loadBanner(
        parent: ShimmerFrameLayout,
        container: FrameLayout,
        isCollapsible: Boolean = false,
        gravity: BannerGravity = BannerGravity.BOTTOM
    ) {
        if (isLoading) return
        collapsiblePositionType = gravity
        val adSize = getAdSize(activity, parent)
        loadWithFloor(parent,container, adSize, isCollapsible)
    }

    private fun loadWithFloor(
        parent: ShimmerFrameLayout,
        container: FrameLayout,
        adSize: AdSize,
        isCollapsible: Boolean
    ) {
        if (adUnitIdFloor == null) {
            load(adUnitId, parent, container,adSize, isCollapsible)
            return
        }

        load(adUnitIdFloor, parent, container,adSize, isCollapsible, object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "Floor ID Failed: $adUnitIdFloor | Error: ${error.message}")
                Log.i(TAG, "Switching to Normal ID: $adUnitId")
                load(adUnitId, parent, container,adSize, isCollapsible)
            }
        })
    }

    private fun load(
        targetAdUnitId: String,
        parent: ShimmerFrameLayout,
        container: FrameLayout,
        adSize: AdSize,
        isCollapsible: Boolean,
        internalListener: AdListener? = null
    ) {
        destroy()
        resetTracker()
        isLoading = true

        adView = AdView(context).apply {
            setAdSize(adSize)
            setAdUnitId(targetAdUnitId)

            val request = buildAdRequest(isCollapsible)

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    this@BannerAdHelper.isLoading = false
                    Log.i(TAG, "Loaded OK: $targetAdUnitId")
                    adView = this@apply

                    container.removeAllViews()
                    container.addView(this@apply)
                    parent.hideShimmer()

                    internalListener?.onAdLoaded()

                    val responseExtras = responseInfo?.responseExtras
                    Log.i(TAG, "Ad Loaded Successfully!")
                    Log.d(
                        TAG,
                        "Mediation Adapter: ${responseInfo?.loadedAdapterResponseInfo?.adSourceName}"
                    )
                    Log.d(TAG, "Response Extras: $responseExtras")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    this@BannerAdHelper.isLoading = false
                    internalListener?.onAdFailedToLoad(error)
                    Log.e(TAG, "Ad Failed to Load: $targetAdUnitId")
                    Log.e(TAG, "Error Code: ${error.code} | Message: ${error.message}")
                    container.removeAllViews()
                    parent.hideShimmer()
                }

                override fun onAdImpression() {
                    Log.d(TAG, "Ad Impression Logged")
                    logImpression(getMediationName(responseInfo))
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Ad Clicked!")
                    super.onAdClicked()
                }
            }

            setOnPaidEventListener { onPaid(it, responseInfo) }

            Log.d(TAG, "Calling loadAd for ID: $targetAdUnitId")
            loadAd(request)
        }
    }

    private fun buildAdRequest(isCollapsible: Boolean): AdRequest {
        val builder = AdRequest.Builder()
        if (isCollapsible) {
            val bundle = Bundle().apply {
                putString(
                    "collapsible",
                    if (collapsiblePositionType != BannerGravity.BOTTOM) "top" else "bottom"
                )
            }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
        }
        return builder.build()
    }

    fun getAdSize(activity: Activity, container: View): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val density = displayMetrics.density

        var adWidthPixels = container.width.toFloat()
        if (adWidthPixels == 0f) adWidthPixels = displayMetrics.widthPixels.toFloat()

        val adWidth = (adWidthPixels / density).toInt()

        if (adWidth <= 0) Log.e(
            TAG,
            "Warning: adWidth is 0 or negative. Adaptive Banner might fail."
        )

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun destroy() {
        Log.d(TAG, "Destroying AdView")
        adView?.destroy()
        adView = null
        isLoading = false
    }
}
