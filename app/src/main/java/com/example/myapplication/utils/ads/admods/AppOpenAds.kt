package com.example.myapplication.utils.ads.admods

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.example.myapplication.utils.ads.base.BaseAds
import com.example.myapplication.utils.ads.interfaces.AdLoadCallback
import com.example.myapplication.utils.ads.interfaces.AdShowCallback
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAds : BaseAds<AppOpenAd>() {
    companion object {
        private const val TAG = "TAG_AppOpenResume"
    }

    override fun loadAd(
        context: Context,
        adUnitId: String?,
        callback: AdLoadCallback?
    ) {
    }

    override fun showAd(
        activity: Activity,
        container: FrameLayout?,
        callback: AdShowCallback?
    ) {
    }


}