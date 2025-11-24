package com.example.myapplication.utils.ads

import android.app.Activity
import android.content.Context
import com.example.myapplication.App
import com.example.myapplication.libads.admods.RewardedAds
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener

class RewardedAdsUtil(
    private val context: Context,
    private val idAds: String,
    private val idAds2: String? = null,
    private val isEnable: Boolean
) {

    private var adsController: RewardedAds? = null
    private var isLoading = false

    /** Public load (no callbacks) */
    fun load() {
        if (!isEnable || isLoading) return
        isLoading = true

        if (idAds2 != null) {
            loadInternal(idAds2) { loadInternal(idAds) }
        } else {
            loadInternal(idAds)
        }
    }

    /** Internal load */
    private fun loadInternal(adUnitId: String, onFail: (() -> Unit)? = null) {
        adsController = RewardedAds(context, adUnitId)

        adsController?.load(onAdmobLoadListener = object : OnAdmobLoadListener {
            override fun onLoad() {
                isLoading = false
            }
            override fun onError(e: String) {
                isLoading = false
                onFail?.invoke()
            }
        })
    }

    /**
     * Show Rewarded → return result as a simple Boolean:
     * true  = user earned reward
     * false = fail / close / no reward
     */
    fun show(activity: Activity, result: (Boolean) -> Unit) {
        if (!isEnable) {
            result(false)
            return
        }

        val controller = adsController ?: run {
            result(false)
            return
        }

        controller.show(activity, object : OnAdmobShowListener {
            override fun onShow() {
                result(true)
                reload()
            }

            override fun onError(e: String) {
                result(false)
                reload()
            }
        })

        adsController = null
    }

    /** Auto reload */
    private fun reload() {
        load()
    }

    fun isLoaded(): Boolean = adsController?.loaded() == true
}
