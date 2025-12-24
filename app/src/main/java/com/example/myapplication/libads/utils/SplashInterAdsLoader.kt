package com.example.myapplication.libads.utils

import android.app.Activity
import android.util.Log
import com.example.myapplication.libads.admobs.InterstitialAdHelper
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.interfaces.InterAdsCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
class SplashInterAdsLoader(
    private val activity: Activity,
    private val interSplash2f: Pair<String, AdPlacement>? = null,
    private val interSplash: Pair<String, AdPlacement>? = null,
    private val timeoutMillis: Long = 30_000,
    private val onFinish: () -> Unit
) {

    companion object {
        private const val TAG = "SplashInterAdsLoader"
    }

    private var timeoutJob: Job? = null
    private var isFinished = false
    private val adQueue = mutableListOf<Pair<String, AdPlacement>>()
    private val adsConfig = FirebaseConfigManager.instance().adConfig
    fun start() {
        buildQueue()

        if (adQueue.isEmpty()) {
            finish()
            return
        }

        startTimeout()
        loadNextAd()
    }

    private fun buildQueue() {

        if (adsConfig.enableInterSplash2f && interSplash2f != null) {
            adQueue.add(interSplash2f)
        }
        if (adsConfig.enableInterSplash && interSplash != null) {
            adQueue.add(interSplash)
        }
    }

    private fun loadNextAd() {
        if (adQueue.isEmpty()) {
            Log.i(TAG, "All inter failed → finish")
            finish()
            return
        }

        val (adUnitId, placement) = adQueue.removeAt(0)
        Log.i(TAG, "Loading inter: $adUnitId")

        val interAds = InterstitialAdHelper(activity, adUnitId, placement)

        interAds.load(object : InterAdsCallback {

            override fun onLoadSuccess() {
                cancelTimeout()
                interAds.show(activity, this)
            }

            override fun onLoadFailed(error: String) {
                Log.i(TAG, "Load failed [$adUnitId]: $error")
                loadNextAd()
            }

            override fun onShowSuccess() {
                Log.i(TAG, "Show success [$adUnitId]")
            }

            override fun onShowFailed(error: String) {
                Log.i(TAG, "Show failed [$adUnitId]: $error")
                loadNextAd()
            }

            override fun onAdClosed() {
                Log.i(TAG, "Ad closed [$adUnitId]")
                finish()
            }
        })
    }

    private fun startTimeout() {
        timeoutJob?.cancel()
        timeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(timeoutMillis)
            Log.i(TAG, "Splash timeout → finish")
            finish()
        }
    }

    private fun cancelTimeout() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    private fun finish() {
        if (isFinished) return
        isFinished = true
        cancelTimeout()
        onFinish.invoke()
    }
}
