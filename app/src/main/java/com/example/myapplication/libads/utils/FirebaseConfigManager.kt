package com.example.myapplication.libads.utils

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson

class FirebaseConfigManager {

    companion object {
        const val KEY_ADS_STATUS_ENABLE = "ad_config"

        private var mInstance: FirebaseConfigManager = FirebaseConfigManager()
        fun instance(): FirebaseConfigManager {
            return mInstance
        }
    }

    private val mMaxTryTime = 5
    private var mTryTime = 0

    var adConfig: AdsConfig = AdsConfig()

    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    fun fetch(onFetchComplete: (() -> Unit)? = null) {
        val longCache = 15 * 60L
        FirebaseRemoteConfig.getInstance().fetch(longCache).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseRemoteConfig.getInstance().activate()
                val json = FirebaseRemoteConfig.getInstance().getString(KEY_ADS_STATUS_ENABLE)
                Log.i("TAG_ad_config", "fetch: ${json}")
                if (json.isEmpty()) {
                    tryFetchAgain(onFetchComplete)
                } else {
                    try {
                        adConfig = Gson().fromJson(json, AdsConfig::class.java)
                        onFetchComplete?.invoke()
                    } catch (e: Exception) {
                        tryFetchAgain(onFetchComplete)
                    }
                }

            } else {
                tryFetchAgain(onFetchComplete)
                Log.e("TAG_ad_config", "Load failed")
            }
        }
    }

    private fun tryFetchAgain(onFetchComplete: (() -> Unit)? = null) {
        ++mTryTime
        if (mTryTime < mMaxTryTime) {
            fetch(onFetchComplete)
        } else {
            mTryTime = 0
            onFetchComplete?.invoke()
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return try {
            remoteConfig.getString(key).ifEmpty { defaultValue }
        } catch (e: Exception) {
            Log.e("TAG_ad_config", "getString() error: ${e.message}")
            defaultValue
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            remoteConfig.getBoolean(key)
        } catch (e: Exception) {
            Log.e("TAG_ad_config", "getBoolean() error: ${e.message}")
            defaultValue
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return try {
            remoteConfig.getLong(key)
        } catch (e: Exception) {
            Log.e("TAG_ad_config", "getLong() error: ${e.message}")
            defaultValue
        }
    }
}