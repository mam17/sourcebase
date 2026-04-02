package com.example.myapplication.libads.utils

import android.annotation.SuppressLint
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.libads.admods.NativeAds
import com.example.myapplication.libads.firebase.FirebaseConfigManager
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener

object NativeAdsUtil {
    @SuppressLint("StaticFieldLeak")
    var splashNativeFullAdmob: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var languageNativeAdmob1: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var homeNativeAdmob: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var languageNativeAdmob2: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var onbNativeFullScreenAdmob: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var onbNativeFullScreenAdmob2: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var featureNativeAdmob: NativeAds? = null

    fun loadNativeFullSplash(onAdLoaded: ((NativeAds) -> Unit)? = null) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        val config = FirebaseConfigManager.instance().adConfig
        if (!config.native_fs_splash.enabled && !config.native_fs_splash_2f.enabled) return

        val id2F = config.native_fs_splash_2f.id
        val id1F = config.native_fs_splash.id

        loadWithFallback(
            idPrimary = id2F,
            idFallback = id1F,
            adPlacement = AdPlacement.NATIVE_FS_SPLASH,
            onLoaded = {
                splashNativeFullAdmob = it
                Log.d("TAG_ADS_Native", "Splash Native loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "Splash Native: all IDs failed")
            }
        )
    }

    fun loadNativeLanguage1(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_language_1_1 else config.native_language_2_1

        if (!adUnit.enabled) return

        val id1F =
            adUnit.id

        loadWithFallback(
            idPrimary = id1F,
            adPlacement = AdPlacement.NATIVE_LANGUAGE1,
            onLoaded = {
                languageNativeAdmob1 = it
                Log.d("TAG_ADS_Native", "loadNativeLanguage1 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "loadNativeLanguage1: all IDs failed")
            }
        )
    }

    fun loadNativeLanguage2(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = if (isFirstOpenApp) config.native_language_1_2 else config.native_language_2_2

        if (!adUnit.enabled) return

        val idMain = adUnit.id

        loadWithFallback(
            idPrimary = idMain,
            adPlacement = AdPlacement.NATIVE_LANGUAGE2,
            onLoaded = {
                languageNativeAdmob2 = it
                Log.d("TAG_ADS_Native", "loadNativeLanguage2 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "loadNativeLanguage2: all IDs failed")
            }
        )
    }


//    fun loadNativeOnboarding1(isShowAds: Boolean, isFirstOpenApp: Boolean) {
//        if (!isShowAds) return
//
//        val id1F = if (isFirstOpenApp) BuildConfig.native_onboarding_1 else BuildConfig.native_onboarding_2_1
//
//        loadWithFallback(
//            idPrimary = id1F,
//            adPlacement = AdPlacement.NATIVE_ONBOARDING,
//            onLoaded = {
//                onboardingNativeAdmob1 = it
//                Log.d("TAG_ADS_Native", "loadNativeOnboarding1 loaded!")
//            },
//            onFailed = {
//                Log.d("TAG_ADS_Native", "loadNativeOnboarding1: all IDs failed")
//            }
//        )
//    }

    fun loadNativeFullScreenOnb(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        val config = FirebaseConfigManager.instance().adConfig
        if (!config.native_fs_1_1.enabled && !config.native_fs_1_2f.enabled && !config.native_fs_2_1.enabled && !config.native_fs_2_1f.enabled) return

        val id1F = if (isFirstOpenApp) { config.native_fs_1_1.id } else { config.native_fs_2_1.id }
        val id2F = if (isFirstOpenApp) { config.native_fs_1_2f.id } else { config.native_fs_2_1f.id }

        loadWithFallback(
            idPrimary = id2F,
            idFallback = id1F,
            adPlacement = AdPlacement.NATIVE_FULL_SCREEN_ONBOARDING,
            onLoaded = {
                onbNativeFullScreenAdmob = it
                Log.d("TAG_ADS_Native", "loadNativeFullScreenOnb loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "loadNativeFullScreenOnb: all IDs failed")
            }
        )
    }

    fun loadNativeFullScreenOnb2(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        val config = FirebaseConfigManager.instance().adConfig
        if (!config.native_fs_2_1.enabled && !config.native_fs_2_2.enabled && !config.native_fs_2_2f.enabled) return

        val id1F = if (isFirstOpenApp) { config.native_fs_2_1.id } else { config.native_fs_2_2.id }

        val id2F = config.native_fs_2_2f.id

        loadWithFallback(
            idPrimary = id2F,
            idFallback = id1F,
            adPlacement = AdPlacement.NATIVE_FULL_SCREEN_ONBOARDING2,
            onLoaded = {
                onbNativeFullScreenAdmob2 = it
                Log.d("TAG_ADS_Native", "loadNativeFullScreenOnb2 loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "loadNativeFullScreenOnb2: all IDs failed")
            }
        )
    }

    fun loadNativeFeature(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = config.native_feature

        if (!adUnit.enabled) return

        val idMain = adUnit.id

        loadWithFallback(
            idPrimary = idMain,
            adPlacement = AdPlacement.NATIVE_PERMISSION,
            onLoaded = {
                featureNativeAdmob = it
                Log.d("TAG_ADS_Native", "loadNativeFeature loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "loadNativeFeature: all IDs failed")
            }
        )
    }

//    fun loadNativeFeature(isFirstOpenApp: Boolean) {
//        val config = FirebaseConfigManager.instance().adConfig
//        if (!config.native_feature.enabled) return
//
//        val id1F = config.native_feature.id.ifEmpty { BuildConfig.native_feature }
//
//        loadWithFallback(
//            idPrimary = id1F,
//            adPlacement = AdPlacement.NATIVE_PERMISSION,
//            onLoaded = {
//                featureNativeAdmob = it
//                Log.d("TAG_ADS_Native", "loadNativeFeature loaded!")
//            },
//            onFailed = {
//                Log.d("TAG_ADS_Native", "loadNativeFeature: all IDs failed")
//            }
//        )
//    }

    fun loadNativeHome(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        if (!FirebaseConfigManager.instance().isEnableAllAds) return
        val config = FirebaseConfigManager.instance().adConfig
        Log.i("TAG_ADS_Native", "loadNativeHome: id1F $config")

        val adUnit = config.native_home

        if (!adUnit.enabled) return

        val id1F = adUnit.id

        loadWithFallback(
            idPrimary = id1F,
            adPlacement = AdPlacement.NATIVE_HOME,
            onLoaded = {
                homeNativeAdmob = it
                Log.d("TAG_ADS_Native", "loadNativeHome loaded!")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "loadNativeHome: all IDs failed")
            }
        )
    }

    fun loadWithFallback(
        idPrimary: String,
        idFallback: String? = null,
        adPlacement: String,
        onLoaded: (NativeAds) -> Unit,
        onFailed: (() -> Unit)? = null
    ) {
        fun loadAd(id: String,next: (() -> Unit)? = null) {
            if (id.isEmpty()) {
                next?.invoke() ?: onFailed?.invoke()
                return
            }

            val finalId = AdsEx.getNativeId(id)

            App.instance?.applicationContext?.let { context ->
                val nativeAds = NativeAds(context, finalId, adPlacement)
                nativeAds.load(object : OnAdmobLoadListener {
                    override fun onLoad() {
                        onLoaded(nativeAds)
                    }

                    override fun onError(e: String) {
                        next?.invoke() ?: onFailed?.invoke()
                    }
                })
            }
        }

        loadAd(idPrimary) {
            if (idFallback != null) {
                loadAd(idFallback, null)
            } else {
                onFailed?.invoke()
            }
        }
    }
}