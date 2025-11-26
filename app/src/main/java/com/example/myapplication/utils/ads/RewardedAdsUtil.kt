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
    private val adPlacement: String,
    private val isEnable: Boolean
) {

    private var adsController: RewardedAds? = null
    private var isLoading = false

    fun load(callback: OnAdmobLoadListener) {
        if (!isEnable) {
            callback.onError("Ad disabled: $adPlacement")
            return
        }
        if (isLoading) return
        isLoading = true

        if (idAds2 != null) {
            loadInternal(idAds2, callback) {
                loadInternal(idAds, callback, null)
            }
        } else {
            loadInternal(idAds, callback, null)
        }
    }

    private fun loadInternal(adUnitId: String, callback: OnAdmobLoadListener, onFail: (() -> Unit)?) {
        adsController = RewardedAds(context, adUnitId, adPlacement)
        adsController?.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                isLoading = false
                callback.onLoad()
            }

            override fun onError(e: String) {
                isLoading = false
                onFail?.invoke() ?: callback.onError(e)
            }
        })
    }

    fun show(activity: Activity, listener: OnAdmobShowListener) {
        if (!isEnable) {
            listener.onError("Ad disabled: $adPlacement")
            return
        }

        val controller = adsController ?: run {
            listener.onError("Ad not loaded")
            return
        }
        App.isInterstitialShowing = true

        controller.show(activity, object : OnAdmobShowListener {
            override fun onShow() {
                App.isInterstitialShowing = false

                listener.onShow()
                load(object : OnAdmobLoadListener {
                    override fun onLoad() {}
                    override fun onError(e: String) {}
                })
            }

            override fun onError(e: String) {
                App.isInterstitialShowing = false

                listener.onError(e)
            }
        })

        adsController = null
    }

    fun isLoaded(): Boolean = adsController?.loaded() == true
}
