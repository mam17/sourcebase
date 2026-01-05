package com.example.myapplication.libads.events

import com.example.myapplication.libads.data.AdRevenueData
import com.example.myapplication.libads.utils.AdPlacement
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo

object AdRevenueMapper {

    fun fromAdmob(
        adValue: AdValue,
        placement: AdPlacement,
        adType: String,
        responseInfo: ResponseInfo?
    ): AdRevenueData? {

        if (adValue.valueMicros <= 0) return null

        val mediation = responseInfo
            ?.loadedAdapterResponseInfo
            ?.adSourceName ?: "AdMob"

        return AdRevenueData(
            valueMicros = adValue.valueMicros,
            currency = adValue.currencyCode,
            adType = adType,
            placement = placement.value,
            mediation = mediation,
            precision = adValue.precisionType
        )
    }
}
