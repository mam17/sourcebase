package com.example.myapplication.utils.ads.admods

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.example.myapplication.utils.ads.base.BaseAds
import com.example.myapplication.utils.ads.interfaces.AdLoadCallback
import com.example.myapplication.utils.ads.interfaces.AdShowCallback
import com.google.android.gms.ads.rewarded.RewardedAd

class RewardedAds : BaseAds<RewardedAd>() {
    companion object {
        private const val TAG = "TAG_RewardedAds"
    }
    override fun loadAd(
        context: Context,
        adUnitId: String?,
        callback: AdLoadCallback?
    ) {
        TODO("Not yet implemented")
    }

    override fun showAd(
        activity: Activity,
        container: FrameLayout?,
        callback: AdShowCallback?
    ) {
        TODO("Not yet implemented")
    }


}