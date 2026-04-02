package com.example.myapplication.libads.firebase

import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.libads.helper.AdConfig
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.SpManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson

class FirebaseConfigManager private constructor() {

    companion object {
        @Volatile
        private var mInstance: FirebaseConfigManager? = null

        fun instance(): FirebaseConfigManager {
            return mInstance ?: synchronized(this) {
                mInstance ?: FirebaseConfigManager().also { mInstance = it }
            }
        }
    }

    private val mMaxTryTime = 5
    private var mTryTime = 0

    var adConfig: AdConfig = AdConfig()
    var isEnableAllAds: Boolean = true

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 Hour
                .build()
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(mapOf(
                Constant.KEY_REMOTE_ENABLE_ALL_ADS to true,
                Constant.KEY_REMOTE_AD_CONFIG to Gson().toJson(AdConfig())
            ))
        }
    }

    init {
        loadLocalConfig()
    }

    fun loadLocalConfig() {
        val app = App.instance
        if (app == null) {
            Log.e("TAG_FirebaseConfigManager", "loadLocalConfig failed: App.instance is null")
            return
        }

        val spManager = SpManager.getInstance(app)
        isEnableAllAds = spManager.getBoolean(Constant.KEY_REMOTE_ENABLE_ALL_ADS, true)
        val savedConfig = spManager.getObject(Constant.KEY_REMOTE_AD_CONFIG, AdConfig::class.java)
        
        if (savedConfig != null) {
            adConfig = savedConfig
            Log.d("TAG_FirebaseConfigManager", "Loaded config from local: ${Gson().toJson(adConfig)}")
        } else {
            Log.w("TAG_FirebaseConfigManager", "No config found in local, using defaults")
        }
    }

    fun fetch(onFetchComplete: (() -> Unit)? = null) {
        // Đảm bảo load local ít nhất một lần trước khi fetch
        if (adConfig.native_home.id.isEmpty()) {
            loadLocalConfig()
        }

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newJson = remoteConfig.getString(Constant.KEY_REMOTE_AD_CONFIG)
                val newEnableAllAds = remoteConfig.getBoolean(Constant.KEY_REMOTE_ENABLE_ALL_ADS)

                Log.d("TAG_FirebaseConfigManager", "Fetch successful. New JSON: $newJson")

                if (newJson.isNotEmpty()) {
                    updateAndSaveConfig(newJson, newEnableAllAds)
                }
                onFetchComplete?.invoke()
            } else {
                Log.e("TAG_FirebaseConfigManager", "Fetch failed: ${task.exception?.message}")
                tryFetchAgain(onFetchComplete)
            }
        }
    }

    private fun updateAndSaveConfig(json: String, enableAllAds: Boolean) {
        val app = App.instance ?: return
        val spManager = SpManager.getInstance(app)
        val oldJson = spManager.getString(Constant.KEY_REMOTE_AD_CONFIG, "")
        val oldEnableAllAds = spManager.getBoolean(Constant.KEY_REMOTE_ENABLE_ALL_ADS, true)

        if (json != oldJson || enableAllAds != oldEnableAllAds) {
            try {
                val config = Gson().fromJson(json, AdConfig::class.java)
                adConfig = config
                isEnableAllAds = enableAllAds
                
                spManager.putString(Constant.KEY_REMOTE_AD_CONFIG, json)
                spManager.putBoolean(Constant.KEY_REMOTE_ENABLE_ALL_ADS, enableAllAds)
                
                Log.d("TAG_FirebaseConfigManager", "Config updated and saved to local!")
            } catch (e: Exception) {
                Log.e("TAG_FirebaseConfigManager", "Parse config error: ${e.message}")
            }
        } else {
            // Ngay cả khi json không đổi, vẫn gán lại vào memory nếu memory đang trống
            if (adConfig.native_home.id.isEmpty()) {
                try {
                    adConfig = Gson().fromJson(json, AdConfig::class.java)
                } catch (_: Exception) {}
            }
            Log.d("TAG_FirebaseConfigManager", "No change in config, skip saving.")
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
            defaultValue
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            remoteConfig.getBoolean(key)
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return try {
            remoteConfig.getLong(key)
        } catch (e: Exception) {
            defaultValue
        }
    }
}