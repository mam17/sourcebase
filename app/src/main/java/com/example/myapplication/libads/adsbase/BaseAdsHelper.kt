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

    protected fun onPaid(adValue: AdValue, responseInfo: ResponseInfo?) {
        logPaidOnce {
            // Tất cả logic bên trong lambda này chỉ chạy 1 lần nhờ BaseAdTracker
            AdRevenueMapper.fromAdmob(
                adValue = adValue,
                placement = adPlacement,
                adType = adType,
                responseInfo = responseInfo
            )?.let { data ->
                // 1. Gửi sang MMP (AppsFlyer, FB, TikTok)
                adRevenueTracker.track(data)

                // 2. Gửi sang Firebase chuẩn (Nên thêm vào để xem báo cáo doanh thu)
                FirebaseAnalytics.getInstance(context).logEvent(
                    FirebaseAnalytics.Event.AD_IMPRESSION,
                    bundleOf(
                        FirebaseAnalytics.Param.VALUE to data.revenue,
                        FirebaseAnalytics.Param.CURRENCY to data.currency,
                        FirebaseAnalytics.Param.AD_PLATFORM to "admob",
                        FirebaseAnalytics.Param.AD_SOURCE to data.mediation,
                        FirebaseAnalytics.Param.AD_FORMAT to adType,
                        FirebaseAnalytics.Param.AD_UNIT_NAME to adPlacement.value
                    )
                )
            }
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
            // Logic log sự kiện hiển thị (Show Success)
            FirebaseAnalytics.getInstance(context).logEvent(
                "ad_impression_custom",
                bundleOf(
                    "ad_unit_name" to adPlacement.value,
                    "ad_format" to adType,
                    "mediation" to (mediation ?: "AdMob")
                )
            )
        }
    }

    protected fun getMediationName(responseInfo: ResponseInfo?): String {
        return responseInfo
            ?.loadedAdapterResponseInfo
            ?.adSourceName
            ?: "AdMob"
    }
}