package com.example.myapplication.libads.adsbase

import android.content.Context
import androidx.core.os.bundleOf
import com.example.myapplication.libads.events.AdRevenueMapper
import com.example.myapplication.libads.events.AdRevenueTracker
import com.example.myapplication.libads.utils.AdPlacement
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.firebase.analytics.FirebaseAnalytics

abstract class BaseAdsHelper(
    protected val context: Context,
    protected val adPlacement: AdPlacement,
    protected val adType: String
) : BaseAdTracker(){

    private val adRevenueTracker = AdRevenueTracker(context)

    private var paidLogged = false
    private var impressionLogged = false

    override fun resetTracker() {
        paidLogged = false
        impressionLogged = false
    }

    /* ================= PAID EVENT ================= */

    protected fun onPaid(
        adValue: AdValue,
        responseInfo: ResponseInfo?
    ) {
        if (paidLogged) return
        paidLogged = true

        AdRevenueMapper.fromAdmob(
            adValue = adValue,
            placement = adPlacement,
            adType = adType,
            responseInfo = responseInfo
        )?.let {
            adRevenueTracker.track(it)
        }
    }

    /* ================= IMPRESSION ================= */

    protected fun onImpression(
        block: () -> Unit
    ) {
        if (impressionLogged) return
        impressionLogged = true
        block()
    }

    protected fun logImpression(mediation: String?) {
        logImpressionOnce {
            FirebaseAnalytics.getInstance(context).logEvent(
                "ad_impression",
                bundleOf(
                    "ad_platform" to "admob",
                    "ad_unit_name" to adPlacement.value,
                    "ad_format" to adType,
                    "mediation" to (mediation ?: "unknown")
                )
            )
        }
    }

    protected fun getMediationName(responseInfo: ResponseInfo?): String {
        return responseInfo
            ?.loadedAdapterResponseInfo
            ?.adSourceName
            ?: "unknown"
    }
}