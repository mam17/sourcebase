package com.example.myapplication.libads.events

import android.content.Context
import android.os.Bundle
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.example.myapplication.libads.data.AdRevenueData
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.tiktok.TikTokBusinessSdk
import com.tiktok.appevents.base.EventName
import com.tiktok.appevents.base.TTBaseEvent
import com.tiktok.appevents.contents.TTContentsEventConstants
import com.tiktok.appevents.contents.TTPurchaseEvent
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRevenueTracker @Inject constructor(
    private val context: Context
) {

    // Khởi tạo logger một lần để tối ưu bộ nhớ
    private val fbLogger: AppEventsLogger by lazy {
        AppEventsLogger.newLogger(context)
    }

    fun track(data: AdRevenueData) {
        if (data.revenue <= 0) return

        trackFacebook(data)
        trackAppsFlyer(data)
        trackTikTok(data)
    }

    private fun trackFacebook(data: AdRevenueData) {
        val params = Bundle().apply {
            putString(AppEventsConstants.EVENT_PARAM_AD_TYPE, data.adType)
            putString(AppEventsConstants.EVENT_PARAM_CURRENCY, data.currency)
            putString("ad_placement", data.placement)
            putString("ad_mediation", data.mediation)
            // Gửi thêm precision để Facebook đánh giá chất lượng traffic
            putInt("ad_precision", data.precision)
        }

        // Đối với doanh thu quảng cáo, Facebook khuyến nghị dùng event này thay vì logPurchase thông thường
        fbLogger.logEvent(
            AppEventsConstants.EVENT_NAME_AD_IMPRESSION,
            data.revenue,
            params
        )
    }

    private fun trackAppsFlyer(data: AdRevenueData) {
        val afData = AFAdRevenueData(
            monetizationNetwork = data.mediation,
            mediationNetwork = MediationNetwork.GOOGLE_ADMOB,
            currencyIso4217Code = data.currency,
            revenue = data.revenue
        )

        val additionalParameters = mapOf(
            AdRevenueScheme.AD_TYPE to data.adType,
            "placement" to data.placement,
            "precision" to data.precision.toString() // Quan trọng cho thuật toán của AppsFlyer
        )

        AppsFlyerLib.getInstance().logAdRevenue(afData, additionalParameters)
    }

    private fun trackTikTok(data: AdRevenueData) {
        // TikTok khuyến nghị làm tròn đến 3 hoặc 6 chữ số thập phân
        val value = BigDecimal(data.revenue).setScale(6, RoundingMode.HALF_UP)

        // 1. Track Ad Impression Event
        val impression = TTBaseEvent.newBuilder(EventName.IN_APP_AD_IMPR.toString())
            .addProperty("currency", data.currency)
            .addProperty("value", value)
            .addProperty("content_id", data.placement)
            .addProperty("content_type", data.adType)
            .addProperty("mediation_name", data.mediation)
            .build()
        TikTokBusinessSdk.trackTTEvent(impression)

        // 2. Track Purchase Event cho TikTok (Tùy chọn)
        // Lưu ý: TikTok yêu cầu truyền Enum Currency chính xác
        val purchase = TTPurchaseEvent.newBuilder("Ad_Revenue_Purchase")
            .setContentId(data.placement)
            .setContentType(data.adType)
            .setCurrency(mapToTikTokCurrency(data.currency))
            .setValue(value.toDouble())
            .build()

        TikTokBusinessSdk.trackTTEvent(purchase)
    }

    /**
     * Helper để map string currency sang Enum của TikTok
     */
    private fun mapToTikTokCurrency(currencyCode: String): TTContentsEventConstants.Currency {
        return when (currencyCode.uppercase()) {
            "VND" -> TTContentsEventConstants.Currency.VND
            "EUR" -> TTContentsEventConstants.Currency.EUR
            else -> TTContentsEventConstants.Currency.USD
        }
    }
}
