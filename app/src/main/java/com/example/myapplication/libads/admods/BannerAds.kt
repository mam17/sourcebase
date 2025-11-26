package com.example.myapplication.libads.admods

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.myapplication.libads.base.BaseAds
import com.example.myapplication.libads.event.MMPManager.logAdRevenue
import com.example.myapplication.libads.helper.CollapsiblePositionType
import com.facebook.appevents.AppEventsLogger
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import java.math.BigDecimal
import java.util.Currency

class BannerAds(
    context: Context,
    private val collapsiblePositionType: CollapsiblePositionType,
    private val adPlacement: String = ""
) : BaseAds(context) {

    companion object {
        private const val TAG = "tag_bannerAdmob"
    }

    private var adView: AdView? = null

    fun showBanner(
        activity: Activity,
        id: String,
        parent: ShimmerFrameLayout
    ) {
        Log.i(TAG, "showBanner: $id")

        val adSize = getAdSize(activity, parent)
        adView = AdView(activity).apply {
            setAdSize(adSize)
            adUnitId = id

            adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.i(TAG, "onAdFailedToLoad: ${loadAdError.message}")
                    parent.removeAllViews()
                }

                override fun onAdLoaded() {
                    Log.i(TAG, "onAdLoaded")
                    parent.removeAllViews()
                    parent.addView(this@apply)
                    parent.hideShimmer()

                    setOnPaidEventListener { adValue ->
                        val revenue = adValue.valueMicros / 1_000_000.0
                        if (revenue > 0) {
                            AppEventsLogger.newLogger(context).logPurchase(
                                BigDecimal.valueOf(revenue),
                                Currency.getInstance("USD")
                            )
                        }

                        context.logAdRevenue(
                            adValue = adValue,
                            adUnitId = adPlacement,
                            responseInfo = adView?.responseInfo,
                            adType = "ad_banner"
                        )
                    }
                }
            }
        }

        val bundle = Bundle()

        when (collapsiblePositionType) {
            CollapsiblePositionType.NONE -> {}
            CollapsiblePositionType.TOP -> {
                bundle.putString("collapsible", "top")
                adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
            }

            CollapsiblePositionType.BOTTOM -> {
                bundle.putString("collapsible", "bottom")
                adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
            }
        }

        adView?.loadAd(adRequestBuilder.build())
    }

    fun showBannerWithFallback(
        activity: Activity,
        primaryAdUnitId: String,
        secondaryAdUnitId: String,
        parent: ShimmerFrameLayout
    ) {
        Log.i(TAG, "Loading secondary/floor ad first: $secondaryAdUnitId")

        val adSize = getAdSize(activity, parent)

        adView = AdView(activity).apply {
            setAdSize(adSize)
            adUnitId = secondaryAdUnitId

            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.i(TAG, "Secondary ad loaded successfully")
                    parent.removeAllViews()
                    parent.addView(this@apply)
                    parent.hideShimmer()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i(TAG, "Secondary ad failed, fallback to primary: $primaryAdUnitId")
                    showBanner(activity, primaryAdUnitId, parent)
                }
            }
        }

        // Load secondary
        adView?.loadAd(adRequestBuilder.build())
    }


    fun getAdSize(activity: Activity, container: View): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val density = displayMetrics.density
        var adWidthPixels = container.width.toFloat()

        if (adWidthPixels == 0f) {
            adWidthPixels = displayMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun onResume() {
        adView?.resume()
    }

    fun onPause() {
        adView?.pause()
    }

    fun onDestroy() {
        adView?.destroy()
    }

    fun reload() {
        adView?.loadAd(adRequestBuilder.build())
    }
}
