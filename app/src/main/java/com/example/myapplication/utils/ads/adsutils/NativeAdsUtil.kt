package com.example.myapplication.utils.ads.adsutils

import android.annotation.SuppressLint
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.utils.ads.admods.NativeAds

object NativeAdsUtil {
    @SuppressLint("StaticFieldLeak")
    var homeNativeAdmob: NativeAds? = null



    fun loadNativeHome() {
        App.instance?.applicationContext?.let { context ->
            homeNativeAdmob = NativeAds(
                context,
                BuildConfig.native_home
            )
            homeNativeAdmob?.load(null)
        }
    }


}