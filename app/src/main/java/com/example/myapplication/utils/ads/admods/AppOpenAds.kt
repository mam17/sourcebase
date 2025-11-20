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
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import java.math.BigDecimal
import java.util.*

class AppOpenAds(
    context: Context,
    private val id: String
) : BaseAds(context) {

    companion object {
        private const val TAG = "TAG_openAdmob"
    }

    private var isLoadingAd = false
    private var appOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0

    fun loadAd(activity: Activity) {
        loadAd(activity, null)
    }

    fun loadAd(activity: Activity, listener: OnAdmobLoadListener?) {
        Log.i(TAG, "loadAd()")

        if (isLoadingAd || isAdAvailable()) {
            Log.i(TAG, "isLoadingAd || isAdAvailable → skip load")
            return
        }

        isLoadingAd = true

        AppOpenAd.load(
            activity,
            id,
            adRequestBuilder.build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.i(TAG, "onAdLoaded")
                    isLoadingAd = false
                    appOpenAd = ad
                    loadTime = Date().time
                    listener?.onLoad()

                    appOpenAd?.setOnPaidEventListener { adValue ->
                        val revenue = adValue.valueMicros / 1_000_000.0
                        if (revenue > 0) {
                            AppEventsLogger.newLogger(context).logPurchase(
                                BigDecimal.valueOf(revenue),
                                Currency.getInstance("USD")
                            )
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.i(TAG, "onAdFailedToLoad: ${error.message}")
                    isLoadingAd = false
                    listener?.onError(error.message)
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(hours: Long): Boolean {
        val diff = Date().time - loadTime
        val millisPerHour = 3600000L
        return diff < millisPerHour * hours
    }

    fun showAdAppOnResume(currentActivity: Activity?) {
        if (currentActivity == null || currentActivity.isFinishing || currentActivity.isDestroyed) {
            Log.i(TAG, "Activity not valid → skip showing AppOpen")
            return
        }

        if (isShowingOpenAd || !canShowOpenApp) {
            Log.i(TAG, "isShowingOpenAd || !canShowOpenApp → cannot show")
            return
        }

        if (!isAdAvailable()) {
            Log.i(TAG, "!isAdAvailable → loadAd()")
            loadAd(currentActivity)
            return
        }

        val ad = appOpenAd ?: return

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.i(TAG, "onAdDismissedFullScreenContent")
                latestTimeShowOpenAd = System.currentTimeMillis()
                appOpenAd = null
                isShowingOpenAd = false
                loadAd(currentActivity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.i(TAG, "onAdFailedToShowFullScreenContent: ${adError.message}")
                appOpenAd = null
                isShowingOpenAd = false
                loadAd(currentActivity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.i(TAG, "onAdShowedFullScreenContent")
                isShowingOpenAd = true
            }
        }

        // Dialog loading full screen
        val dialog = Dialog(currentActivity).apply {
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
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Dialog show error: ${e.message}")
            ad.show(currentActivity)
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (!currentActivity.isFinishing && !currentActivity.isDestroyed) {
                dialog.dismiss()
                ad.show(currentActivity)
            }
        }, 1500)
    }

}