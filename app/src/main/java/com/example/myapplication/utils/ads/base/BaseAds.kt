package com.example.myapplication.utils.ads.base

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.example.myapplication.BuildConfig
import com.example.myapplication.utils.ads.interfaces.OnAdmobLoadListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.security.MessageDigest

@SuppressLint("HardwareIds")
open class BaseAds(val context: Context) {

    companion object {
        var canShowOpenApp: Boolean = true
        var isShowingOpenAd: Boolean = false
        var latestTimeShowOpenAd: Long = 0

        fun md5(s: String): String {
            return try {
                val digest = MessageDigest.getInstance("MD5")
                val result = digest.digest(s.toByteArray())
                buildString {
                    result.forEach {
                        append(String.format("%02x", it))
                    }
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    var adRequestBuilder: AdRequest.Builder
    var onAdmobLoadListener: OnAdmobLoadListener? = null

    init {
        MobileAds.initialize(context) {}

        var deviceId = ""

        if (BuildConfig.DEBUG) {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            deviceId = md5(androidId).uppercase()
        }

        val config = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(deviceId))
            .build()

        MobileAds.setRequestConfiguration(config)

        adRequestBuilder = AdRequest.Builder()
            .setRequestAgent(AdRequest.DEVICE_ID_EMULATOR)
            .setRequestAgent(deviceId)
    }

//    interface OnAdmobLoadListener {
//        fun onLoad()
//        fun onError(e: String)
//    }
//
//    interface OnAdmobShowListener {
//        fun onShow()
//        fun onError(e: String)
//    }
}
