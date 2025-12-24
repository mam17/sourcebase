package com.example.myapplication.libads.utils

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AdsConfig (
    // -------- Interstitial Ads --------
    @SerializedName("inter_splash")
    @Expose var enableInterSplash: Boolean = true,

    @SerializedName("inter_splash_2f")
    @Expose var enableInterSplash2f: Boolean = true,
)