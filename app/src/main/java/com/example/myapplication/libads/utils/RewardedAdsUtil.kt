package com.example.myapplication.libads.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.libads.admods.RewardedAds
import com.example.myapplication.libads.firebase.FirebaseConfigManager
import com.example.myapplication.libads.dialog.DialogLoadingAds
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener
import com.example.myapplication.utils.SpManager

class RewardedAdsUtil(
    private val context: Context,
    private val idAds: String,
    private val idAds2: String? = null,
    private val adPlacement: String,
    private val isEnable: Boolean
) {

    private var adsController: RewardedAds? = null
    private var isLoading = false

    // Pending show
    private var pendingActivity: Activity? = null
    private var pendingListener: OnAdmobShowListener? = null
    private var dialogLoading: DialogLoadingAds? = null

    fun load(callback: OnAdmobLoadListener) {
        if (SpManager.getInstance(context).isPro()) {
            callback.onLoad()
            return
        }
        if (!isEnable || !FirebaseConfigManager.instance().isEnableAllAds) {
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

    private fun loadInternal(
        adUnitId: String,
        callback: OnAdmobLoadListener,
        onFail: (() -> Unit)?
    ) {
        adsController = RewardedAds(context, adUnitId, adPlacement)
        adsController?.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                isLoading = false
                callback.onLoad()

                // Check pending show
                if (pendingActivity != null && pendingListener != null) {
                    val act = pendingActivity!!
                    val lst = pendingListener!!
                    pendingActivity = null
                    pendingListener = null
                    show(act, lst)
                }
            }

            override fun onError(e: String) {
                // Only treat as final error if no failover (onFail is null)
                if (onFail == null) {
                    isLoading = false
                }

                onFail?.invoke() ?: run {
                    callback.onError(e)
                    // If we had a pending show and this was the last attempt, fail it too
                    if (pendingActivity != null && pendingListener != null) {
                        pendingListener?.onError(e)
                        pendingActivity = null
                        pendingListener = null
                    }
                }
            }
        })
    }

    fun show(activity: Activity, listener: OnAdmobShowListener, autoLoadAndShow: Boolean = false) {
        if (SpManager.getInstance(context).isPro()) {
            listener.onShow()
            listener.onClosed()
            return
        }

        if (!isEnable || !FirebaseConfigManager.instance().isEnableAllAds) {
            listener.onError("Ad disabled: $adPlacement")
            return
        }

        val controller = adsController
        if (controller == null || !controller.loaded()) {
            if (autoLoadAndShow) {
                pendingActivity = activity
                pendingListener = listener
                
                showLoadingDialog(activity)
                
                load(object : OnAdmobLoadListener {
                    override fun onLoad() {
                        dismissLoadingDialog()
                    }
                    override fun onError(e: String) {
                        dismissLoadingDialog()
                    }
                })
            } else {
                Log.d("RewardedAdsUtil", "Ad not ready and autoLoadAndShow is false, skipping - $adPlacement")
                listener.onError("Ad not ready")
                // Load bù cho lần sau
                load(object : OnAdmobLoadListener {
                    override fun onLoad() {}
                    override fun onError(e: String) {}
                })
            }
            return
        }
        App.isInterstitialShowing = true

        controller.show(activity, object : OnAdmobShowListener {
            override fun onShow() {
                App.isInterstitialShowing = false

                listener.onShow()
            }

            override fun onError(e: String) {
                App.isInterstitialShowing = false

                listener.onError(e)
            }

            override fun onClosed() {
                App.isInterstitialShowing = false
                listener.onClosed()
            }
        })

        adsController = null
    }

    private fun showLoadingDialog(activity: Activity) {
        dismissLoadingDialog()
        try {
            if (!activity.isFinishing && !activity.isDestroyed) {
                dialogLoading = DialogLoadingAds(activity)
                dialogLoading?.show()

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    dismissLoadingDialog()
                }, 10000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dismissLoadingDialog() {
        try {
            dialogLoading?.dismiss()
        } catch (_: Exception) {
        } finally {
            dialogLoading = null
        }
    }

    fun isLoaded(): Boolean = adsController?.loaded() == true
}