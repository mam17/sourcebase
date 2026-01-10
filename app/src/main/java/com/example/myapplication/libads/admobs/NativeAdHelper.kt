package com.example.myapplication.libads.admobs

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.AdUnifiedBinding
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class NativeAdHelper(
    private val activity: Activity,
    private val adUnitId: String,
    private val adUnitIdFloor: String? = null,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = activity,
    adPlacement = adPlacement,
    adType = "native"
) {

    private var currentNativeAd: NativeAd? = null
    private var isLoading = false

    companion object {
        private const val TAG = "NativeAdHelper"
    }

    fun loadNativeAd(
        parent: ViewGroup,
        shimmer: ShimmerFrameLayout? = null
    ) {
        if (isLoading) return
        isLoading = true

        shimmer?.startShimmer()

        loadWithFloor(parent, shimmer)
    }

    private fun loadWithFloor(
        parent: ViewGroup,
        shimmer: ShimmerFrameLayout?
    ) {
        if (adUnitIdFloor == null) {
            load(adUnitId, parent, shimmer)
            return
        }

        load(adUnitIdFloor, parent, shimmer, object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "Floor ID Failed: $adUnitIdFloor | Error: ${error.message}")
                load(adUnitId, parent, shimmer)
            }
        })
    }

    private fun load(
        targetAdUnitId: String,
        parent: ViewGroup,
        shimmer: ShimmerFrameLayout?,
        internalListener: AdListener? = null
    ) {
        resetTracker()
        val builder = AdLoader.Builder(activity, targetAdUnitId)

        builder.forNativeAd { nativeAd ->
            isLoading = false
            if (isActivityDestroyed()) {
                nativeAd.destroy()
                return@forNativeAd
            }

            currentNativeAd?.destroy()
            currentNativeAd = nativeAd

            val adBinding = AdUnifiedBinding.inflate(LayoutInflater.from(activity))
            populateNativeAdView(nativeAd, adBinding)

            parent.removeAllViews()
            parent.addView(adBinding.root)

            shimmer?.stopShimmer()
            shimmer?.hideShimmer()
            
            internalListener?.onAdLoaded()
            Log.i(TAG, "Native Ad Loaded: $targetAdUnitId")
        }

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                isLoading = false
                Log.e(TAG, "Native Ad Failed: $targetAdUnitId | Error: ${error.message}")
                
                if (adUnitIdFloor == null || targetAdUnitId == adUnitId) {
                    shimmer?.stopShimmer()
                    shimmer?.hideShimmer()
                }
                
                internalListener?.onAdFailedToLoad(error)
            }

            override fun onAdImpression() {
                Log.d(TAG, "Native Ad Impression Logged")
                logImpression(getMediationName(currentNativeAd?.responseInfo))
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "Native Ad Clicked")
            }
        }).build()

        currentNativeAd?.setOnPaidEventListener { adValue ->
             onPaid(adValue, currentNativeAd?.responseInfo)
        }

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adBinding: AdUnifiedBinding) {
        val nativeAdView: NativeAdView = adBinding.root

        // Set the media view.
        nativeAdView.mediaView = adBinding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = adBinding.adHeadline
        nativeAdView.bodyView = adBinding.adBody
        nativeAdView.callToActionView = adBinding.adCallToAction
        nativeAdView.iconView = adBinding.adAppIcon
        nativeAdView.priceView = adBinding.adPrice
        nativeAdView.starRatingView = adBinding.adStars
        nativeAdView.storeView = adBinding.adStore
        nativeAdView.advertiserView = adBinding.adAdvertiser

        // Headline is guaranteed
        adBinding.adHeadline.text = nativeAd.headline
        nativeAd.mediaContent?.let { adBinding.adMedia.setMediaContent(it) }

        // Optional assets
        if (nativeAd.body == null) {
            adBinding.adBody.visibility = View.INVISIBLE
        } else {
            adBinding.adBody.visibility = View.VISIBLE
            adBinding.adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adBinding.adCallToAction.visibility = View.INVISIBLE
        } else {
            adBinding.adCallToAction.visibility = View.VISIBLE
            adBinding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adBinding.adAppIcon.visibility = View.GONE
        } else {
            adBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            adBinding.adAppIcon.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adBinding.adPrice.visibility = View.INVISIBLE
        } else {
            adBinding.adPrice.visibility = View.VISIBLE
            adBinding.adPrice.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adBinding.adStore.visibility = View.INVISIBLE
        } else {
            adBinding.adStore.visibility = View.VISIBLE
            adBinding.adStore.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adBinding.adStars.visibility = View.INVISIBLE
        } else {
            adBinding.adStars.rating = nativeAd.starRating!!.toFloat()
            adBinding.adStars.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adBinding.adAdvertiser.visibility = View.INVISIBLE
        } else {
            adBinding.adAdvertiser.text = nativeAd.advertiser
            adBinding.adAdvertiser.visibility = View.VISIBLE
        }

        // Must call this
        nativeAdView.setNativeAd(nativeAd)

        // Paid Event
        nativeAd.setOnPaidEventListener { adValue ->
            onPaid(adValue, nativeAd.responseInfo)
        }
    }

    private fun isActivityDestroyed(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.isDestroyed || activity.isFinishing
        } else {
            activity.isFinishing
        }
    }

    fun destroy() {
        currentNativeAd?.destroy()
        currentNativeAd = null
    }
}
