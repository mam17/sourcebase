package com.example.myapplication.libads.admobs

import android.app.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.BannerGravity
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*

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

    companion object {
        private const val TAG = "BannerAdHelper"
    }

    fun loadBanner(
        container: ViewGroup,
        isCollapsible: Boolean = false,
        gravity: BannerGravity = BannerGravity.BOTTOM
    ) {
        if (isLoading) return

        val adSize = getAdSize(activity, container)

        // Log thông tin AdSize - Cực kỳ quan trọng vì Collapsible chỉ chạy với Adaptive Size
        Log.d(TAG, "--- Start Load Banner ---")
        Log.d(TAG, "AdSize: $adSize | Width: ${adSize.width} | IsCollapsible: $isCollapsible")

        loadWithFloor(container, adSize, isCollapsible, gravity)
    }

    private fun loadWithFloor(
        container: ViewGroup,
        adSize: AdSize,
        isCollapsible: Boolean,
        gravity: BannerGravity
    ) {
        if (adUnitIdFloor == null) {
            load(adUnitId, container, adSize, isCollapsible, gravity)
            return
        }

        load(adUnitIdFloor, container, adSize, isCollapsible, gravity, object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "Floor ID Failed: $adUnitIdFloor | Error: ${error.message}")
                Log.i(TAG, "Switching to Normal ID: $adUnitId")
                load(adUnitId, container, adSize, isCollapsible, gravity)
            }
        })
    }

    private fun load(
        targetAdUnitId: String,
        container: ViewGroup,
        adSize: AdSize,
        isCollapsible: Boolean,
        gravity: BannerGravity,
        internalListener: AdListener? = null
    ) {
        destroy()
        resetTracker()
        isLoading = true

        adView = AdView(context).apply {
            setAdSize(adSize)
            setAdUnitId(targetAdUnitId)

            // Log AdRequest details
            val adRequest = if (isCollapsible) {
                val extras = Bundle().apply {
                    putString("collapsible", gravity.value)
                }
                Log.d(TAG, "Requesting Collapsible Banner with gravity: ${gravity.value}")
                AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                    .build()
            } else {
                AdRequest.Builder().build()
            }

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    this@BannerAdHelper.isLoading = false
                    container.removeAllViews()
                    container.addView(this@apply)
                    internalListener?.onAdLoaded()

                    // Kiểm tra xem thực tế AdMob có trả về Collapsible không
                    // Banner Collapsible thường có extras đặc biệt trong response
                    val responseExtras = responseInfo?.responseExtras
                    Log.i(TAG, "Ad Loaded Successfully!")
                    Log.d(TAG, "Mediation Adapter: ${responseInfo?.loadedAdapterResponseInfo?.adSourceName}")
                    Log.d(TAG, "Response Extras: $responseExtras")

                    this@apply.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    this@BannerAdHelper.isLoading = false
                    internalListener?.onAdFailedToLoad(error)
                    Log.e(TAG, "Ad Failed to Load: $targetAdUnitId")
                    Log.e(TAG, "Error Code: ${error.code} | Message: ${error.message}")
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
            loadAd(adRequest)
        }
    }

    fun getAdSize(activity: Activity, container: View): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val density = displayMetrics.density

        var adWidthPixels = container.width.toFloat()
        if (adWidthPixels == 0f) adWidthPixels = displayMetrics.widthPixels.toFloat()

        val adWidth = (adWidthPixels / density).toInt()

        // Nếu adWidth quá nhỏ (do container chưa đo xong), AdMob có thể không trả về Collapsible
        if (adWidth <= 0) Log.e(TAG, "Warning: adWidth is 0 or negative. Adaptive Banner might fail.")

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun destroy() {
        Log.d(TAG, "Destroying AdView")
        adView?.destroy()
        adView = null
        isLoading = false
    }
}