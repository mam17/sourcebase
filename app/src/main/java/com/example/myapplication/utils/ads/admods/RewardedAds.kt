package com.example.myapplication.utils.ads.admods


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.example.myapplication.R
import com.example.myapplication.utils.ads.base.BaseAds
import com.example.myapplication.utils.ads.interfaces.OnAdmobLoadListener
import com.example.myapplication.utils.ads.interfaces.OnAdmobShowListener
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.math.BigDecimal
import java.util.Currency

class RewardedAds(
    context: Context,
    private val id: String
) : BaseAds(context) {

    companion object {
        private const val TAG = "TAG_reward"
    }

    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var earnReward = false

    init {
        Log.i(TAG, "RewardAdmob: $id")
    }

    // ===========================================================
    //                     LOAD REWARDED
    // ===========================================================
    fun load(onAdmobLoadListener: OnAdmobLoadListener?) {
        Log.i(TAG, "load rewarded")
        isLoading = true

        RewardedAd.load(
            context,
            id,
            adRequestBuilder.build(),
            object : RewardedAdLoadCallback() {

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    onAdmobLoadListener?.onError(error.message)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    onAdmobLoadListener?.onLoad()

                    ad.setOnPaidEventListener { value ->
                        val revenue = value.valueMicros / 1_000_000.0
                        if (revenue > 0) {
                            AppEventsLogger.newLogger(context)
                                .logPurchase(
                                    BigDecimal.valueOf(revenue),
                                    Currency.getInstance("USD")
                                )
                        }
                    }
                }
            }
        )
    }

    // ===========================================================
    //                       SHOW REWARDED
    // ===========================================================
    fun show(activity: Activity, onAdmobShowListener: OnAdmobShowListener) {
        Log.i(TAG, "show rewarded")
        earnReward = false

        val ad = rewardedAd
        if (ad == null) {
            onAdmobShowListener.onError("null")
            return
        }

        // Loading dialog
        val dialog = Dialog(activity).apply {
            setContentView(R.layout.layout_loading_ads)

            window?.setBackgroundDrawableResource(android.R.color.transparent)

            window?.let { w ->
                val params = w.attributes
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.MATCH_PARENT
                params.gravity = Gravity.CENTER
                w.attributes = params
            }

            setCancelable(false)
        }

        try {
            if (!activity.isDestroyed && !dialog.isShowing) {
                dialog.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dialog show error")
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!activity.isDestroyed) {
                dialog.dismiss()

                val rewarded = rewardedAd
                if (rewarded == null || activity.isDestroyed) {
                    onAdmobShowListener.onError("Ad not ready")
                    return@postDelayed
                }

                rewarded.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdShowedFullScreenContent() {
                        // nothing
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        rewardedAd = null
                        onAdmobShowListener.onError(adError.message)
                    }

                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        if (earnReward) {
                            onAdmobShowListener.onShow()
                        } else {
                            onAdmobShowListener.onError("no reward")
                        }
                    }
                }

                rewarded.show(activity) {
                    earnReward = true
                }
            }
        }, 2000)
    }

    // ===========================================================
    //                       CHECK LOADED
    // ===========================================================
    fun loaded(): Boolean {
        Log.i(TAG, "loaded: ${rewardedAd != null}")
        return rewardedAd != null
    }
}