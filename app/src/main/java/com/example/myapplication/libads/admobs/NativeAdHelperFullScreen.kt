package com.example.myapplication.libads.admobs

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

class NativeAdHelperFullScreen(
    private val activity: Activity,
    private val adUnitId: String,
    private val adUnitIdFloor: String? = null,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = activity,
    adPlacement = adPlacement,
    adType = "native_fullscreen"
) {

    private var currentNativeAd: NativeAd? = null
    private var isLoading = false

    companion object {
        private const val TAG = "NativeFullScreen"
    }

    /**
     * Load và hiển thị Native Full Screen vào parent
     */
    fun loadAndShow(
        parent: ViewGroup,
        shimmer: ShimmerFrameLayout? = null,
        onAdLoaded: (() -> Unit)? = null,
        onAdFailed: (() -> Unit)? = null
    ) {
        if (isLoading) return
        isLoading = true

        shimmer?.visibility = View.VISIBLE
        shimmer?.startShimmer()

        loadWithFloor(parent, shimmer, onAdLoaded, onAdFailed)
    }

    private fun loadWithFloor(
        parent: ViewGroup,
        shimmer: ShimmerFrameLayout?,
        onAdLoaded: (() -> Unit)?,
        onAdFailed: (() -> Unit)?
    ) {
        if (adUnitIdFloor == null) {
            load(adUnitId, parent, shimmer, onAdLoaded, onAdFailed)
            return
        }

        load(adUnitIdFloor, parent, shimmer, onAdLoaded, onAdFailed, object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "Floor ID Failed: $adUnitIdFloor | Error: ${error.message}")
                load(adUnitId, parent, shimmer, onAdLoaded, onAdFailed)
            }
        })
    }

    private fun load(
        targetAdUnitId: String,
        parent: ViewGroup,
        shimmer: ShimmerFrameLayout?,
        onAdLoaded: (() -> Unit)?,
        onAdFailed: (() -> Unit)?,
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

            // Inflate layout full screen của bạn
            val nativeAdView = LayoutInflater.from(activity)
                .inflate(R.layout.ad_unified_full_screen, null) as NativeAdView
            
            populateNativeAdView(nativeAd, nativeAdView)

            parent.removeAllViews()
            parent.addView(nativeAdView)

            shimmer?.stopShimmer()
            shimmer?.hideShimmer()
            
            onAdLoaded?.invoke()
            internalListener?.onAdLoaded()
            Log.i(TAG, "Native FullScreen Loaded: $targetAdUnitId")
        }

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        // Đối với Full Screen, thường để Aspect Ratio là ANY để tối ưu diện tích Media
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                isLoading = false
                Log.e(TAG, "Native FullScreen Failed: $targetAdUnitId | Error: ${error.message}")
                
                if (adUnitIdFloor == null || targetAdUnitId == adUnitId) {
                    shimmer?.stopShimmer()
                    shimmer?.hideShimmer()
                    onAdFailed?.invoke()
                }
                
                internalListener?.onAdFailedToLoad(error)
            }

            override fun onAdImpression() {
                Log.d(TAG, "Native FullScreen Impression Logged")
                logImpression(getMediationName(currentNativeAd?.responseInfo))
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "Native FullScreen Clicked")
            }
        }).build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        val adMedia: MediaView? = nativeAdView.findViewById(R.id.ad_media)
        val adHeadline: TextView? = nativeAdView.findViewById(R.id.ad_headline)
        val adBody: TextView? = nativeAdView.findViewById(R.id.ad_body)
        val adCallToAction: View? = nativeAdView.findViewById(R.id.ad_call_to_action)
        val adAppIcon: ImageView? = nativeAdView.findViewById(R.id.ad_app_icon)
        val adPrice: TextView? = nativeAdView.findViewById(R.id.ad_price)
        val adStars: RatingBar? = nativeAdView.findViewById(R.id.ad_stars)
        val adStore: TextView? = nativeAdView.findViewById(R.id.ad_store)
        val adAdvertiser: TextView? = nativeAdView.findViewById(R.id.ad_advertiser)

        nativeAdView.mediaView = adMedia
        nativeAdView.headlineView = adHeadline
        nativeAdView.bodyView = adBody
        nativeAdView.callToActionView = adCallToAction
        nativeAdView.iconView = adAppIcon
        nativeAdView.priceView = adPrice
        nativeAdView.starRatingView = adStars
        nativeAdView.storeView = adStore
        nativeAdView.advertiserView = adAdvertiser

        adHeadline?.text = nativeAd.headline
        nativeAd.mediaContent?.let { adMedia?.setMediaContent(it) }

        if (nativeAd.body == null) {
            adBody?.visibility = View.INVISIBLE
        } else {
            adBody?.visibility = View.VISIBLE
            adBody?.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adCallToAction?.visibility = View.INVISIBLE
        } else {
            adCallToAction?.visibility = View.VISIBLE
            if (adCallToAction is TextView) {
                adCallToAction.text = nativeAd.callToAction
            }
        }

        if (nativeAd.icon == null) {
            adAppIcon?.visibility = View.GONE
        } else {
            adAppIcon?.setImageDrawable(nativeAd.icon?.drawable)
            adAppIcon?.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adPrice?.visibility = View.INVISIBLE
        } else {
            adPrice?.visibility = View.VISIBLE
            adPrice?.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adStore?.visibility = View.INVISIBLE
        } else {
            adStore?.visibility = View.VISIBLE
            adStore?.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adStars?.visibility = View.INVISIBLE
        } else {
            adStars?.rating = nativeAd.starRating!!.toFloat()
            adStars?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adAdvertiser?.visibility = View.INVISIBLE
        } else {
            adAdvertiser?.text = nativeAd.advertiser
            adAdvertiser?.visibility = View.VISIBLE
        }

        nativeAdView.setNativeAd(nativeAd)

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
