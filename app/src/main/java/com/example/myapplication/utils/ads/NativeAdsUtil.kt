package com.example.myapplication.utils.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.libads.admods.NativeAds
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener

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

//    fun loadNativeFullSplash(activity: Activity, isShowAds: Boolean, isFirstOpenApp: Boolean) {
//        if (!isShowAds) return
//
//        val id2F = if (isFirstOpenApp) BuildConfig.native_fs_splash_2f else ""
//        val id1F = if (isFirstOpenApp) BuildConfig.native_fs_splash else BuildConfig.native_fs_splash_2_1
//
//        loadWithFallback(
//            context = activity,
//            idPrimary = id2F,
//            idFallback = id1F,
//            adPlacement = AdPlacement.NATIVE_FS_SPLASH,
//            onLoaded = {
//                splashNativeFullAdmob = it
//                Log.d("NativeAdsUtil", "Splash Native loaded!")
//            },
//            onFailed = {
//                Log.d("NativeAdsUtil", "Splash Native: all IDs failed")
//            }
//        )
//    }
    fun loadWithFallback(
        context: Context,
        idPrimary: String,
        idFallback: String? = null,
        adPlacement: String,
        onLoaded: (NativeAds) -> Unit,
        onFailed: (() -> Unit)? = null
    ) {
        fun loadAd(id: String, next: (() -> Unit)? = null) {
            val nativeAds = NativeAds(context, id, adPlacement)

            nativeAds.load(object : OnAdmobLoadListener {
                override fun onLoad() {
                    onLoaded(nativeAds)
                }

                override fun onError(e: String) {
                    next?.invoke() ?: onFailed?.invoke()
                }
            })
        }

        if (idFallback != null) {
            loadAd(idPrimary) {
                loadAd(idFallback, null)
            }
        } else {
            loadAd(idPrimary, null)
        }
    }
}