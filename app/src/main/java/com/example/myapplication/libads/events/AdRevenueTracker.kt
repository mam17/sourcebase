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

    fun track(data: AdRevenueData) {
        if (data.revenue <= 0) return

        trackFacebook(data)
        trackAppsFlyer(data)
        trackTikTok(data)
    }
    private fun trackFacebook(data: AdRevenueData) {
        AppEventsLogger.newLogger(context)
            .logPurchase(
                BigDecimal.valueOf(data.revenue),
                Currency.getInstance(data.currency)
            )

        AppEventsLogger.newLogger(context)
            .logEvent(
                AppEventsConstants.EVENT_NAME_AD_IMPRESSION,
                data.revenue,
                Bundle().apply {
                    putString(AppEventsConstants.EVENT_PARAM_CURRENCY, data.currency)
                }
            )
    }

    private fun trackAppsFlyer(data: AdRevenueData) {
        val afData = AFAdRevenueData(
            monetizationNetwork = data.mediation,
            mediationNetwork = MediationNetwork.GOOGLE_ADMOB,
            currencyIso4217Code = data.currency,
            revenue = data.revenue
        )

        AppsFlyerLib.getInstance().logAdRevenue(
            afData,
            mapOf(
                AdRevenueScheme.AD_TYPE to data.adType,
                "placement" to data.placement
            )
        )
    }
    private fun trackTikTok(data: AdRevenueData) {
        val value = BigDecimal(data.revenue).setScale(3, RoundingMode.HALF_UP)

        val impression = TTBaseEvent.newBuilder(EventName.IN_APP_AD_IMPR.toString())
            .addProperty("currency", data.currency)
            .addProperty("value", value)
            .addProperty("content_id", data.placement)
            .addProperty("content_type", data.adType)
            .build()

        TikTokBusinessSdk.trackTTEvent(impression)

        val purchase = TTPurchaseEvent.newBuilder("Purchase")
            .setContentId(data.placement)
            .setContentType(data.adType)
            .setCurrency(TTContentsEventConstants.Currency.USD)
            .setValue(value.toDouble())
            .build()

        TikTokBusinessSdk.trackTTEvent(purchase)
    }

}
