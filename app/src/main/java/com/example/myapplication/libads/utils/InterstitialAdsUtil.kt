package com.example.myapplication.libads.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.libads.admods.InterstitialAds
import com.example.myapplication.libads.dialog.DialogLoadingAds
import com.example.myapplication.libads.firebase.FirebaseConfigManager
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener

class InterstitialAdsUtil(
    private val context: Context,
    private val idAds: String,
    private val idAds2f: String? = null,
    private val adPlacement: String,
    private val isEnable: Boolean,
) {

    companion object {
        private const val TAG = "InterstitialController"
    }

    private var adsController: InterstitialAds? = null
    private var isLoading = false
    private var lastRequestTime: Long = 0
    private val MIN_RETRY_INTERVAL = 5000L

    // Pending show
    private var pendingActivity: Activity? = null
    private var pendingListener: OnAdmobShowListener? = null
    private var dialogLoading: DialogLoadingAds? = null

    fun load(callback: OnAdmobLoadListener? = null) {
        if (com.example.myapplication.utils.SpManager.getInstance(context).isPro()) return
        if (!isEnable || !FirebaseConfigManager.instance().isEnableAllAds) return

        val currentTime = System.currentTimeMillis()
        if (isLoading || (currentTime - lastRequestTime < MIN_RETRY_INTERVAL)) {
            Log.d(TAG, "Load ignored: already loading or requested too recently")
            return
        }

        isLoading = true
        lastRequestTime = currentTime

        if (idAds2f != null) {
            loadInternal(idAds2f, callback) {
                loadInternal(idAds, callback, null)
            }
        } else {
            loadInternal(idAds, callback, null)
        }
    }

    private fun loadInternal(
        adUnitId: String,
        callback: OnAdmobLoadListener?,
        onFail: (() -> Unit)?
    ) {
        adsController = InterstitialAds(context, adUnitId, adPlacement, false)
        adsController?.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                isLoading = false
                Log.d(TAG, "Loaded successfully → $adUnitId")
                callback?.onLoad()

                if (pendingActivity != null && pendingListener != null) {
                    val act = pendingActivity!!
                    val lst = pendingListener!!
                    pendingActivity = null
                    pendingListener = null
                    show(act, lst)
                }
            }

            override fun onError(e: String) {
                isLoading = false
                Log.e(TAG, "Failed: $adUnitId, error: $e")
                onFail?.invoke() ?: callback?.onError(e)
            }
        })
    }

    fun show(activity: Activity, listener: OnAdmobShowListener, autoLoadAndShow: Boolean = false) {
        if (com.example.myapplication.utils.SpManager.getInstance(context).isPro()) {
            listener.onShow()
            listener.onClosed()
            return
        }

        if (!isEnable || !FirebaseConfigManager.instance().isEnableAllAds) {
            listener.onError("Ad disabled")
            return
        }

        if (adsController?.available() == true) {
            performShow(activity, listener)
        } else {
            if (autoLoadAndShow) {
                Log.d(TAG, "Ad not ready, showing dialog and queuing for placement: $adPlacement")
                pendingActivity = activity
                pendingListener = listener

                showLoadingDialog(activity)

                load(object : OnAdmobLoadListener {
                    override fun onLoad() {
                    }

                    override fun onError(e: String) {
                        dismissLoadingDialog()
                    }
                })
            } else {
                Log.d(TAG, "Ad not ready and autoLoadAndShow is false, skipping - $adPlacement")
                listener.onError("Ad not ready")
                load()
            }
        }
    }

    private fun performShow(activity: Activity, listener: OnAdmobShowListener) {
        val controller = adsController ?: return

        if (dialogLoading == null) {
            showLoadingDialog(activity)
        }

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                if (!activity.isFinishing && !activity.isDestroyed) {
                    dismissLoadingDialog()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (activity.isFinishing || activity.isDestroyed) {
                adsController = null
                return@postDelayed
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
                    adsController = null
                }

                override fun onClosed() {
                    App.isInterstitialShowing = false
                    listener.onClosed()
                    adsController = null
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        load()
                    }, 1000)
                }
            })
            adsController = null

        }, 800)
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

    fun isAvailable(): Boolean {
        return adsController?.available() == true
    }
}
