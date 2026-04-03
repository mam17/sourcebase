package com.example.myapplication.libads.utils

import android.annotation.SuppressLint
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.libads.admods.NativeAds
import com.example.myapplication.libads.firebase.FirebaseConfigManager
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.utils.SpManager

object NativeAdsUtil {

    @SuppressLint("StaticFieldLeak")
    var splashNativeFullAdmob: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var languageNativeAdmob1: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var languageNativeAdmob2: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var homeNativeAdmob: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var onbNativeFullScreenAdmob: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var onbNativeFullScreenAdmob2: NativeAds? = null

    @SuppressLint("StaticFieldLeak")
    var featureNativeAdmob: NativeAds? = null


    fun loadNativeFullSplash(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        splashNativeFullAdmob?.destroy()
        val config = FirebaseConfigManager.instance().adConfig

        if (!config.native_fs_splash.enabled &&
            !config.native_fs_splash_2f.enabled
        ) return

        loadNative(
            idPrimary = config.native_fs_splash_2f.id,
            idFallback = config.native_fs_splash.id,
            placement = AdPlacement.NATIVE_FS_SPLASH,
            onLoaded = {
                splashNativeFullAdmob = it
                onAdLoaded?.invoke(it)
            }
        )
    }


    fun loadNativeLanguage1(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        languageNativeAdmob1?.destroy()

        val config = FirebaseConfigManager.instance().adConfig
        val adUnit =
            if (isFirstOpenApp) config.native_language_1_1
            else config.native_language_2_1

        if (!adUnit.enabled) return

        loadNative(
            idPrimary = adUnit.id,
            placement = AdPlacement.NATIVE_LANGUAGE1,
            onLoaded = {
                languageNativeAdmob1 = it
                Log.d("TAG_ADS_Native", "loadNativeLanguage1 loaded")
                onAdLoaded?.invoke(it)
            }
        )
    }


    fun loadNativeLanguage2(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        languageNativeAdmob2?.destroy()

        val config = FirebaseConfigManager.instance().adConfig
        val adUnit =
            if (isFirstOpenApp) config.native_language_1_2
            else config.native_language_2_2

        if (!adUnit.enabled) return

        loadNative(
            idPrimary = adUnit.id,
            placement = AdPlacement.NATIVE_LANGUAGE2,
            onLoaded = {
                languageNativeAdmob2 = it
                Log.d("TAG_ADS_Native", "loadNativeLanguage2 loaded")
                onAdLoaded?.invoke(it)
            }
        )
    }
    fun loadNativeFullScreenOnb(
        isFirstOpenApp: Boolean,
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        onbNativeFullScreenAdmob?.destroy()

        val config = FirebaseConfigManager.instance().adConfig

        if (!config.native_fs_1_1.enabled &&
            !config.native_fs_1_2f.enabled &&
            !config.native_fs_2_1.enabled &&
            !config.native_fs_2_1f.enabled
        ) return

        val id1F = if (isFirstOpenApp) {
            config.native_fs_1_1.id
        } else {
            config.native_fs_2_1.id
        }

        val id2F = if (isFirstOpenApp) {
            config.native_fs_1_2f.id
        } else {
            config.native_fs_2_1f.id
        }

        loadNative(
            idPrimary = id2F,
            idFallback = id1F,
            placement = AdPlacement.NATIVE_FULL_SCREEN_ONBOARDING,
            onLoaded = {
                onbNativeFullScreenAdmob = it
                Log.d("TAG_ADS_Native", "loadNativeFullScreenOnb loaded")
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
        onbNativeFullScreenAdmob2?.destroy()

        val config = FirebaseConfigManager.instance().adConfig

        if (!config.native_fs_2_1.enabled &&
            !config.native_fs_2_2.enabled &&
            !config.native_fs_2_2f.enabled
        ) return

        val id1F = if (isFirstOpenApp) {
            config.native_fs_2_1.id
        } else {
            config.native_fs_2_2.id
        }

        val id2F = config.native_fs_2_2f.id

        loadNative(
            idPrimary = id2F,
            idFallback = id1F,
            placement = AdPlacement.NATIVE_FULL_SCREEN_ONBOARDING2,
            onLoaded = {
                onbNativeFullScreenAdmob2 = it
                Log.d("TAG_ADS_Native", "loadNativeFullScreenOnb2 loaded")
                onAdLoaded?.invoke(it)
            },
            onFailed = {
                Log.d("TAG_ADS_Native", "loadNativeFullScreenOnb2: all IDs failed")
            }
        )
    }
    fun loadNativeHome(
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        homeNativeAdmob?.destroy()

        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = config.native_home

        if (!adUnit.enabled) return

        loadNative(
            idPrimary = adUnit.id,
            placement = AdPlacement.NATIVE_HOME,
            onLoaded = {
                homeNativeAdmob = it
                Log.d("TAG_ADS_Native", "loadNativeHome loaded")
                onAdLoaded?.invoke(it)
            }
        )
    }


    fun loadNativeFeature(
        onAdLoaded: ((NativeAds) -> Unit)? = null
    ) {
        featureNativeAdmob?.destroy()

        val config = FirebaseConfigManager.instance().adConfig
        val adUnit = config.native_feature

        if (!adUnit.enabled) return

        loadNative(
            idPrimary = adUnit.id,
            placement = AdPlacement.NATIVE_PERMISSION,
            onLoaded = {
                featureNativeAdmob = it
                Log.d("TAG_ADS_Native", "loadNativeFeature loaded")
                onAdLoaded?.invoke(it)
            }
        )
    }

    private fun canLoadAds(): Boolean {
        val context = App.instance ?: return false

        if (SpManager.getInstance(context).isPro()) return false
        if (!FirebaseConfigManager.instance().isEnableAllAds) return false

        return true
    }

    private fun loadNative(
        idPrimary: String,
        idFallback: String? = null,
        placement: String,
        onLoaded: (NativeAds) -> Unit,
        onFailed: (() -> Unit)? = null
    ) {

        if (!canLoadAds()) return

        loadWithFallback(
            idPrimary = idPrimary,
            idFallback = idFallback,
            adPlacement = placement,
            onLoaded = onLoaded,
            onFailed = onFailed
        )
    }

    fun destroyAllAds() {
        splashNativeFullAdmob?.destroy()
        splashNativeFullAdmob = null

        languageNativeAdmob1?.destroy()
        languageNativeAdmob1 = null

        languageNativeAdmob2?.destroy()
        languageNativeAdmob2 = null

        homeNativeAdmob?.destroy()
        homeNativeAdmob = null

        onbNativeFullScreenAdmob?.destroy()
        onbNativeFullScreenAdmob = null

        onbNativeFullScreenAdmob2?.destroy()
        onbNativeFullScreenAdmob2 = null

        featureNativeAdmob?.destroy()
        featureNativeAdmob = null
    }

    fun loadWithFallback(
        idPrimary: String,
        idFallback: String? = null,
        adPlacement: String,
        onLoaded: (NativeAds) -> Unit,
        onFailed: (() -> Unit)? = null
    ) {
        if (App.instance?.let { SpManager.getInstance(it).isPro() } == true) return

        fun loadAd(id: String, next: (() -> Unit)? = null) {
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