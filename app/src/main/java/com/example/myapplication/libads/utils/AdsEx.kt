package com.example.myapplication.libads.utils

import android.util.Log
import com.example.myapplication.BuildConfig

object AdsEx {
    const val ID_INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712"
    const val ID_BANNER_TEST = "ca-app-pub-3940256099942544/2014213617"
    const val ID_REWARD_TEST = "ca-app-pub-3940256099942544/5224354917"
    const val ID_NATIVE_TEST = "ca-app-pub-3940256099942544/2247696110"
    const val ID_APP_OPEN_TEST = "ca-app-pub-3940256099942544/9257395921"

    fun getBannerId(realId: String): String {
        return if (BuildConfig.DEBUG || realId.isEmpty()) {
            Log.d("TAG_ADS_AdsEx", "Using Test Banner ID (Debug or Empty Real ID)")
            ID_BANNER_TEST
        } else realId
    }

    fun getInterstitialId(realId: String): String {
        return if (BuildConfig.DEBUG || realId.isEmpty()) {
            Log.d("TAG_ADS_AdsEx", "Using Test Interstitial ID (Debug or Empty Real ID)")
            ID_INTERSTITIAL_TEST
        } else realId
    }

    fun getRewardId(realId: String): String {
        return if (BuildConfig.DEBUG || realId.isEmpty()) {
            Log.d("TAG_ADS_AdsEx", "Using Test Reward ID (Debug or Empty Real ID)")
            ID_REWARD_TEST
        } else realId
    }

    fun getNativeId(realId: String): String {
        return if (BuildConfig.DEBUG || realId.isEmpty()) {
            Log.d("TAG_ADS_AdsEx", "Using Test Native ID (Debug or Empty Real ID)")
            ID_NATIVE_TEST
        } else {
            realId
        }
    }

    fun getAppOpenId(realId: String): String {
        return if (BuildConfig.DEBUG || realId.isEmpty()) {
            Log.d("TAG_ADS_AdsEx", "Using Test AppOpen ID (Debug or Empty Real ID)")
            ID_APP_OPEN_TEST
        } else {
            realId
        }
    }
}