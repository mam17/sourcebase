package com.example.myapplication.utils.ads.interfaces

import com.google.android.gms.ads.LoadAdError

interface AdLoadCallback {
    fun onAdLoaded()
    fun onAdFailed(error: LoadAdError)
}
