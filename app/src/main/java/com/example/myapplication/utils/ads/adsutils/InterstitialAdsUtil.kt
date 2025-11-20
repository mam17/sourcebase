package com.example.myapplication.utils.ads.adsutils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.utils.ads.admods.InterstitialAds
import com.example.myapplication.utils.ads.interfaces.OnAdmobLoadListener
import com.example.myapplication.utils.ads.interfaces.OnAdmobShowListener

class InterstitialAdsUtil(
    private val context: Context,
    private val idAds: String,
    private val idAds2f: String? = null,
    private val adPlacement: String,
    private val isEnable: Boolean
) {

    companion object {
        private const val TAG = "InterstitialController"
    }

    private var adsController: InterstitialAds? = null
    private var isLoading = false

    // Pending show
    private var pendingActivity: Activity? = null
    private var pendingListener: OnAdmobShowListener? = null

    /** Load ads logic với fallback */
    fun load(callback: OnAdmobLoadListener? = null) {
        if (!isEnable) {
            callback?.onError("Ad disabled for placement: $adPlacement")
            return
        }

        if (isLoading) return
        isLoading = true

        if (idAds2f != null) {
            Log.d(TAG, "Trying fallback ID first: $idAds2f")
            loadInternal(idAds2f, callback) {
                Log.d(TAG, "Fallback failed, loading main ID: $idAds")
                loadInternal(idAds, callback, null)
            }
        } else {
            Log.d(TAG, "Loading main ID: $idAds")
            loadInternal(idAds, callback, null)
        }
    }

    /** Internal load */
    private fun loadInternal(
        adUnitId: String,
        callback: OnAdmobLoadListener?,
        onFail: (() -> Unit)?
    ) {
        adsController = InterstitialAds(context, adUnitId)

        adsController?.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                isLoading = false
                Log.d(TAG, "Loaded successfully → $adUnitId, placement: $adPlacement")
                callback?.onLoad()
                pendingActivity?.let { act ->
                    pendingListener?.let { lst ->
                        showPending(act, lst)
                    }
                }
            }

            override fun onError(e: String) {
                isLoading = false
                Log.e(TAG, "Failed: $adUnitId, error: $e")
                onFail?.invoke() ?: callback?.onError(e)
            }
        })
    }

    fun show(activity: Activity, listener: OnAdmobShowListener) {
        if (!isEnable) {
            listener.onError("Ad disabled for placement: $adPlacement")
            return
        }

        val controller = adsController
        if (controller != null && controller.available()) {
            showPending(activity, listener)
        } else {
            Log.d(TAG, "Ad not ready yet, queuing show for placement: $adPlacement")
            pendingActivity = activity
            pendingListener = listener
            if (!isLoading) load()
        }
    }

    private fun showPending(activity: Activity, listener: OnAdmobShowListener) {
        val controller = adsController ?: run {
            listener.onError("Ad not loaded")
            pendingActivity = null
            pendingListener = null
            return
        }

        Log.d(TAG, "Showing interstitial for placement: $adPlacement")
        App.isInterstitialShowing = true
        controller.showInterstitial(activity, object : OnAdmobShowListener {
            override fun onShow() {
                App.isInterstitialShowing = false
                listener.onShow()
            }

            override fun onError(e: String) {
                App.isInterstitialShowing = false
                listener.onError(e)
            }
        })

        adsController = null
        pendingActivity = null
        pendingListener = null

        load()
    }

    fun isAvailable(): Boolean {
        return adsController?.available() == true
    }
}
