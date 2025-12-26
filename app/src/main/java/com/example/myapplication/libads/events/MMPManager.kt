package com.example.myapplication.libads.events

import android.content.Context
import androidx.core.os.bundleOf
import com.example.myapplication.App
import com.example.myapplication.libads.utils.AdPlacement
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.firebase.analytics.FirebaseAnalytics
import java.math.BigDecimal
import java.util.Currency

object MMPManager {

    private const val TAG = "MMPManager"

    fun Context.logAdRevenue(
        adValue: AdValue,
        adUnitId: String = "",
        responseInfo: ResponseInfo? = null,
        adType: String = "unknown"
    ) {
//        (applicationContext as? App)?.handleAdRevenue(adValue, adUnitId, responseInfo, adType)
    }

    fun Context.logInterstitialRevenue(
        adValue: AdValue,
        placement: AdPlacement,
        responseInfo: ResponseInfo?
    ) {
        val revenue = adValue.valueMicros / 1_000_000.0
        if (revenue <= 0) return

        val mediation = responseInfo
            ?.loadedAdapterResponseInfo
            ?.adSourceName ?: "unknown"

        // Firebase
        FirebaseAnalytics.getInstance(this).logEvent(
            "ad_revenue",
            bundleOf(
                "ad_platform" to "admob",
                "ad_unit_name" to placement.value,
                "ad_format" to "interstitial",
                "ad_source" to mediation,
                "value" to revenue,
                "currency" to "USD"
            )
        )

        // Facebook IAP
        AppEventsLogger.newLogger(this).logPurchase(
            BigDecimal.valueOf(revenue),
            Currency.getInstance("USD")
        )
    }
    fun Context.logPaywallShown(source: String) {
        FirebaseAnalytics.getInstance(this)
            .logEvent("paywall_shown", bundleOf("source" to source))
    }
}