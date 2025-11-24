package com.example.myapplication.libads.admods

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.example.myapplication.R
import com.example.myapplication.libads.base.BaseAds
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.math.BigDecimal
import java.util.Currency

class InterstitialAds(
    context: Context,
    private val id: String
) : BaseAds(context) {

    companion object {
        private const val TAG = "TAG_interAdmob"
    }

    private var interstitialAd: InterstitialAd? = null

    fun load(callback: OnAdmobLoadListener) {
        load(callback, 30000)
    }

    fun load(callback: OnAdmobLoadListener, timeoutMillis: Long) {
        Log.i(TAG, "load()")
        onAdmobLoadListener = callback

        if (context == null) {
            callback.onError("null context")
            onAdmobLoadListener = null
            return
        }

        // Timeout handler
        Handler(Looper.getMainLooper()).postDelayed({
            if (interstitialAd == null) {
                onAdmobLoadListener?.onError("error")
                onAdmobLoadListener = null
            }
        }, timeoutMillis)

        InterstitialAd.load(
            context,
            id,
            adRequestBuilder.build(),
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.i(TAG, "onAdLoaded")
                    interstitialAd = ad
                    onAdmobLoadListener?.onLoad()
                    onAdmobLoadListener = null

                    ad.setOnPaidEventListener { adValue ->
                        val revenue = adValue.valueMicros / 1_000_000.0
                        if (revenue > 0) {
                            AppEventsLogger.newLogger(context).logPurchase(
                                BigDecimal.valueOf(revenue),
                                Currency.getInstance("USD")
                            )
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i(TAG, "onAdFailedToLoad: ${error.message}")
                    onAdmobLoadListener?.onError(error.message)
                    onAdmobLoadListener = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, listener: OnAdmobShowListener) {
        Log.i(TAG, "showInterstitial()")

        val ad = interstitialAd
        if (ad == null || isShowingOpenAd) {
            listener.onError("")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                Log.i(TAG, "onAdDismissedFullScreenContent")
                interstitialAd = null
                listener.onShow()
                canShowOpenApp = true
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.i(TAG, "onAdFailedToShowFullScreenContent: ${error.message}")
                interstitialAd = null
                listener.onError(error.message)
            }

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "onAdShowedFullScreenContent")
                canShowOpenApp = false
            }
        }

        val dialog = Dialog(activity).apply {
            setContentView(R.layout.layout_loading_ads)

            window?.setBackgroundDrawableResource(android.R.color.transparent)

            window?.let { w ->
                val params = w.attributes
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.MATCH_PARENT
                params.gravity = Gravity.CENTER
                w.attributes = params
            }

            setCancelable(false)
        }


        try {
            if (!activity.isDestroyed && !dialog.isShowing) dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Dialog error: ${e.message}")
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!activity.isDestroyed) {
                dialog.dismiss()
                interstitialAd?.show(activity) ?: listener.onError("")
            }
        }, 2000)
    }

    fun available(): Boolean {
        val available = interstitialAd != null && !isShowingOpenAd
        Log.i(TAG, "available: $available")
        return available
    }
}
