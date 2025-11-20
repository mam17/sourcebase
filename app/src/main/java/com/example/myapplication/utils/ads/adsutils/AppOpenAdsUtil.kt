package com.example.myapplication.utils.ads.adsutils

import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.example.myapplication.R
import com.example.myapplication.utils.ads.admods.AppOpenAds
import com.example.myapplication.utils.ads.interfaces.OnAdmobLoadListener
import com.example.myapplication.utils.ads.base.BaseAds

class AppOpenAdsUtil(
    private val idAds: String,
    private val idAds2: String? = null,
    private val adPlacement: String,
    private val isEnable: Boolean,
    private val checkOtherAdsShowing: () -> Boolean
) {

    companion object {
        private const val TAG = "AppOpenAdsUtil"
    }

    private var adsController: AppOpenAds? = null
    private var isLoading = false

    fun load(activity: Activity,callback: OnAdmobLoadListener? = null) {
        if (!isEnable) {
            callback?.onError("Ad disabled for placement: $adPlacement")
            return
        }
        if (isLoading) return
        isLoading = true

        val primaryId = idAds2 ?: idAds
        val fallbackId = if (idAds2 != null) idAds else null

        fun loadAdUnit(adUnitId: String, onFail: (() -> Unit)?) {
            adsController = AppOpenAds(activity, adUnitId)
            adsController?.loadAd(activity, object : OnAdmobLoadListener {
                override fun onLoad() {
                    isLoading = false
                    Log.d(TAG, "Loaded → $adUnitId")
                    callback?.onLoad()
                }

                override fun onError(e: String) {
                    Log.d(TAG, "Failed → $adUnitId, error: $e")
                    if (onFail != null) {
                        onFail()
                    } else {
                        isLoading = false
                        callback?.onError(e)
                    }
                }
            })
        }

        if (fallbackId != null) {
            loadAdUnit(primaryId) { loadAdUnit(fallbackId, null) }
        } else {
            loadAdUnit(primaryId, null)
        }
    }

    fun showIfAvailable(activity: Activity) {
        if (!isEnable) return
        if (checkOtherAdsShowing()) {
            Log.d(TAG, "Another ad is showing, skip AppOpen")
            return
        }

        val ad = adsController
        if (ad == null) {
            Log.d(TAG, "AppOpen not loaded → reload")
            load(activity)
            return
        }

        ad.showAdAppOnResume(activity)
        load(activity)
    }

    fun isAvailable(): Boolean = adsController != null
}
