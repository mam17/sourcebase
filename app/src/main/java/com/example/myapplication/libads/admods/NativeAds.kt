package com.example.myapplication.libads.admods

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.BuildConfig
import com.example.myapplication.libads.base.BaseAds
import com.facebook.appevents.AppEventsLogger
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.*
import java.math.BigDecimal
import java.util.*
import com.example.myapplication.R
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener

class NativeAds(
    context: Context,
    private val id: String
) : BaseAds(context) {

    companion object {
        private const val TAG = "Tag_nativeAdmob"
        private var indexLoadNative = 0
    }

    private var indexDebug = 0
    private var canReload = false
    private val handler = Handler(Looper.getMainLooper())

    private val nativeAdLive = MutableLiveData<NativeAd>()

    fun available(): Boolean = nativeAdLive.value != null

    fun getNativeAdLive(): MutableLiveData<NativeAd> = nativeAdLive

    private fun setNativeAd(nativeAd: NativeAd) {
        nativeAdLive.postValue(nativeAd)
    }

    // ========================= SHOW AD ==============================
    fun showNative(
        parent: ShimmerFrameLayout,
        onNativeShowListener: OnAdmobShowListener?
    ) {
        val ad = nativeAdLive.value
        if (ad != null) {
            enableReload(true)

            val adView = parent.findViewById<NativeAdView>(R.id.native_ad_view)

            parent.hideShimmer()
            parent.stopShimmer()

            populateNativeAdView(this, ad, adView)

            onNativeShowListener?.onShow()
        } else {
            onNativeShowListener?.onError("")
        }
    }

    private fun enableReload(b: Boolean) {
        canReload = b
    }

    // ========================= LOAD AD ==============================
    fun load(listener: OnAdmobLoadListener?) {
        load(listener, 30_000)
    }

    fun load(listener: OnAdmobLoadListener?, nativeFull: Boolean) {
        load(listener, 30_000, nativeFull)
    }

    fun load(listener: OnAdmobLoadListener?, timeoutMillis: Long) {
        load(listener, timeoutMillis, false)
    }

    fun load(
        listener: OnAdmobLoadListener?,
        timeoutMillis: Long,
        nativeFull: Boolean
    ) {
        indexDebug = indexLoadNative++
        Log.i(TAG, "NativeAdmob: $id indexDebug: $indexDebug")

        enableReload(false)

        onAdmobLoadListener = listener

        // TIMEOUT
        handler.postDelayed({
            onAdmobLoadListener?.onError("timeout")
            onAdmobLoadListener = null
            Log.i(TAG, "load: timeout")
        }, timeoutMillis)

        val builder = AdLoader.Builder(context, id)

        builder.forNativeAd { ad ->
            Log.i(TAG, "onNativeAdLoaded: ${ad.headline ?: ""}")

            nativeAdLive.value?.destroy()

            ad.setOnPaidEventListener { value ->
                val revenue = value.valueMicros / 1_000_000.0
                if (revenue > 0) {
                    AppEventsLogger.newLogger(context).logPurchase(
                        BigDecimal.valueOf(revenue),
                        Currency.getInstance("USD")
                    )
                }
            }

            setNativeAd(ad)

            onAdmobLoadListener?.onLoad()
            onAdmobLoadListener = null
        }

        val videoOptions = if (nativeFull) {
            VideoOptions.Builder().setStartMuted(false).setCustomControlsRequested(true).build()
        } else {
            VideoOptions.Builder().setStartMuted(true).build()
        }

        val adOptions = NativeAdOptions.Builder()
            .setMediaAspectRatio(MediaAspectRatio.ANY)
            .setVideoOptions(videoOptions)
            .build()

        builder.withNativeAdOptions(adOptions)

        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onAdmobLoadListener?.onError(error.message)
                onAdmobLoadListener = null
            }
        }).build()

        adLoader.loadAd(adRequestBuilder.build())
    }

    fun reLoad() {
        if (!canReload) {
            Log.i(TAG, "not show, disable reload")
            return
        }
        load(null)
    }

    // ========================= POPULATE VIEW ==============================
    fun populateNativeAdView(
        nativeAdmob: NativeAds,
        nativeAd: NativeAd,
        adView: NativeAdView
    ) {
        Log.i(TAG, "populateNativeAdView: ")

        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // HEADLINE
        (adView.headlineView as TextView).text =
            if (BuildConfig.DEBUG)
                nativeAd.headline + nativeAdmob.indexDebug
            else nativeAd.headline

        // MEDIA CONTENT
        try {
            adView.mediaView?.setMediaContent(nativeAd.mediaContent)
        } catch (_: Exception) {
        }

        // BODY
        try {
            if (nativeAd.body == null) {
                adView.bodyView?.visibility = View.INVISIBLE
            } else {
                adView.bodyView?.visibility = View.VISIBLE
                (adView.bodyView as TextView).text = nativeAd.body
            }
        } catch (_: Exception) {
        }

        // CTA
        try {
            if (nativeAd.callToAction == null) {
                adView.callToActionView?.visibility = View.INVISIBLE
            } else {
                adView.callToActionView?.visibility = View.VISIBLE
                (adView.callToActionView as TextView).text = nativeAd.callToAction
            }
        } catch (_: Exception) {
        }

        // ICON
        try {
            if (nativeAd.icon == null) {
                adView.iconView?.visibility = View.GONE
            } else {
                (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                adView.iconView?.visibility = View.VISIBLE
            }
        } catch (_: Exception) {
        }

        // PRICE
        try {
            if (nativeAd.price == null) adView.priceView?.visibility = View.INVISIBLE
            else {
                adView.priceView?.visibility = View.VISIBLE
                (adView.priceView as TextView).text = nativeAd.price
            }
        } catch (_: Exception) {
        }

        // STORE
        try {
            if (nativeAd.store == null) adView.storeView?.visibility = View.INVISIBLE
            else {
                adView.storeView?.visibility = View.VISIBLE
                (adView.storeView as TextView).text = nativeAd.store
            }
        } catch (_: Exception) {
        }

        // STAR RATING
        try {
            if (nativeAd.starRating == null) adView.starRatingView?.visibility = View.INVISIBLE
            else {
                (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
                adView.starRatingView?.visibility = View.VISIBLE
            }
        } catch (_: Exception) {
        }

        // ADVERTISER
        try {
            if (nativeAd.advertiser == null)
                adView.advertiserView?.visibility = View.INVISIBLE
            else {
                (adView.advertiserView as TextView).text = nativeAd.advertiser
                adView.advertiserView?.visibility = View.VISIBLE
            }
        } catch (_: Exception) {
        }

        adView.setNativeAd(nativeAd)

        val vc = nativeAd.mediaContent?.videoController
        vc?.setVideoLifecycleCallbacks(object : VideoController.VideoLifecycleCallbacks() {
            override fun onVideoEnd() {
                super.onVideoEnd()
            }
        })

    }
}