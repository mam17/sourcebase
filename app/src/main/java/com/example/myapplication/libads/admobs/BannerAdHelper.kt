package com.example.myapplication.libads.admobs

import android.content.Context
import android.view.ViewGroup
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.google.android.gms.ads.*

class BannerAdHelper(
    context: Context,
    private val adUnitId: String,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = context,
    adPlacement = adPlacement,
    adType = "banner"
) {

    private var adView: AdView? = null

    fun load(container: ViewGroup, adSize: AdSize) {
        destroy()
        resetTracker()

        adView = AdView(context).apply {
            setAdSize(adSize)
            setAdUnitId(adUnitId)

            setOnPaidEventListener { adValue ->
                onPaid(adValue, responseInfo)
            }

            adListener = object : AdListener() {
                override fun onAdImpression() {
                    onImpression { /* Firebase impression nếu cần */ }
                }
            }
        }

        container.removeAllViews()
        container.addView(adView)
        adView?.loadAd(AdRequest.Builder().build())
    }

    fun destroy() {
        adView?.destroy()
        adView = null
    }
}
