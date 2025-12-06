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
import com.google.android.gms.ads.AdRequest
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


    /** -------------------- PUBLIC: Show banner -------------------- */
    fun showBanner(
        activity: Activity,
        id: String,
        parent: ShimmerFrameLayout
    ) {
        loadBanner(
            activity = activity,
            adUnitId = id,
            parent = parent,
            onFailed = {
                parent.removeAllViews()
                parent.hideShimmer()
            }
        )
    }

    /** -------------------- PUBLIC: Show with fallback -------------------- */
    fun showBannerWithFallback(
        activity: Activity,
        primaryAdUnitId: String,
        secondaryAdUnitId: String,
        parent: ShimmerFrameLayout
    ) {
        loadBanner(
            activity = activity,
            adUnitId = secondaryAdUnitId,
            parent = parent,
            onFailed = {
                Log.i(TAG, "Secondary failed → Try primary")
                loadBanner(
                    activity = activity,
                    adUnitId = primaryAdUnitId,
                    parent = parent,
                    onFailed = {
                        Log.i(TAG, "Primary also failed")
                        parent.removeAllViews()
                        parent.hideShimmer()
                    }
                )
            }
        )
    }


    /** --------------------  CORE LOADER (dùng chung) -------------------- */
    private fun loadBanner(
        activity: Activity,
        adUnitId: String,
        parent: ShimmerFrameLayout,
        onFailed: () -> Unit
    ) {
        val adSize = getAdSize(activity, parent)
        val request = buildAdRequest()

        val adViewLocal = AdView(activity).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId

            adListener = object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i(TAG, "Failed Ad: $adUnitId | ${error.message}")
                    onFailed()
                }

                override fun onAdLoaded() {
                    Log.i(TAG, "Loaded OK: $adUnitId")
                    adView = this@apply
                    parent.removeAllViews()
                    parent.addView(this@apply)
                    parent.hideShimmer()

                    setOnPaidEventListener { adValue ->
                        val revenue = adValue.valueMicros / 1_000_000.0
                        if (revenue > 0) {
                            AppEventsLogger.newLogger(context)
                                .logPurchase(BigDecimal.valueOf(revenue), Currency.getInstance("USD"))
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

        adViewLocal.loadAd(request)
    }


    /** --------------------  Build AdRequest -------------------- */
    private fun buildAdRequest(): AdRequest {
        val builder = AdRequest.Builder()

        if (collapsiblePositionType != CollapsiblePositionType.NONE) {
            val bundle = Bundle().apply {
                putString(
                    "collapsible",
                    if (collapsiblePositionType == CollapsiblePositionType.TOP) "top" else "bottom"
                )
            }
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
        }

        return builder.build()
    }


    /** --------------------  Utils -------------------- */
    fun getAdSize(activity: Activity, container: View): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val density = displayMetrics.density

        var adWidthPixels = container.width.toFloat()
        if (adWidthPixels == 0f) adWidthPixels = displayMetrics.widthPixels.toFloat()

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun onResume() = adView?.resume()
    fun onPause() = adView?.pause()
    fun onDestroy() = adView?.destroy()
    fun reload() = adView?.loadAd(buildAdRequest())
}
