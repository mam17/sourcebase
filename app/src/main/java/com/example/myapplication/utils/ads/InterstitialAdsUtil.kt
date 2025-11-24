package com.example.myapplication.utils.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.libads.admods.InterstitialAds
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener

class InterstitialAdsUtil(
    private val context: Context,
    private val idAds: String,
    private val idAds2f: String? = null,
    private val isEnable: Boolean
) {

    companion object {
        private const val TAG = "InterstitialController"
    }

    private var adsController: InterstitialAds? = null
    private var isLoading = false

    fun load() {
        if (!isEnable || isLoading) return
        isLoading = true

        if (idAds2f != null) {
            loadInternal(idAds2f) { loadInternal(idAds) }
        } else {
            loadInternal(idAds)
        }
    }

    private fun loadInternal(adUnitId: String, onFail: (() -> Unit)? = null) {
        adsController = InterstitialAds(context, adUnitId)

        adsController?.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                isLoading = false
                Log.d(TAG, "Loaded: $adUnitId")
            }

            override fun onError(e: String) {
                isLoading = false
                Log.e(TAG, "Load fail: $e")

                onFail?.invoke()
            }
        })
    }

    fun show(activity: Activity, onDone: (() -> Unit)? = null) {
        if (!isEnable) {
            onDone?.invoke()
            return
        }

        val controller = adsController
        if (controller != null && controller.available()) {
            showInternal(activity, onDone)
        } else {
            Log.d(TAG, "Not ready → loading")
            load()
            onDone?.invoke()
        }
    }

    private fun showInternal(activity: Activity, onDone: (() -> Unit)?) {
        val controller = adsController ?: return

        controller.showInterstitial(activity, object : OnAdmobShowListener {
            override fun onShow() {
                Log.d(TAG, "Show success")
                onDone?.invoke()
            }

            override fun onError(e: String) {
                Log.e(TAG, "Show fail: $e")
                onDone?.invoke()
            }
        })

        adsController = null
        load()
    }

    fun isAvailable(): Boolean {
        return adsController?.available() == true
    }
}
