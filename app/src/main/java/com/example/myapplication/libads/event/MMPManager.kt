package com.example.myapplication.libads.event

import android.content.Context
import com.example.myapplication.App
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo

object MMPManager {

    private const val TAG = "MMPManager"

    fun Context.logAdRevenue(
        adValue: AdValue,
        adUnitId: String = "",
        responseInfo: ResponseInfo? = null,
        adType: String = "unknown"
    ) {
        (applicationContext as? App)?.handleAdRevenue(adValue, adUnitId, responseInfo, adType)
    }
}