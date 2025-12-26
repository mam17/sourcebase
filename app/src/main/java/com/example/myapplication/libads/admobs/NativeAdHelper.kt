package com.example.myapplication.libads.admobs

import android.content.Context
import com.example.myapplication.libads.adsbase.BaseAdsHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class NativeAdHelper(
    context: Context,
    private val adUnitId: String,
    adPlacement: AdPlacement
) : BaseAdsHelper(
    context = context,
    adPlacement = adPlacement,
    adType = "native"
) {

    private var nativeAd: NativeAd? = null

    fun load(onLoaded: (NativeAd) -> Unit) {
        destroy()
        resetTracker()

        val loader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd = ad

                ad.setOnPaidEventListener { adValue ->
                    onPaid(adValue, ad.responseInfo)
                }

                onLoaded(ad)
            }
            .withNativeAdOptions(
                NativeAdOptions.Builder().build()
            )
            .build()

        loader.loadAd(AdRequest.Builder().build())
    }

    fun destroy() {
        nativeAd?.destroy()
        nativeAd = null
    }
}
