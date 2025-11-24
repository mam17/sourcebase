package com.example.myapplication.libads.helper

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AdConfig(

    // -------- App Open Ads --------
    @SerializedName("appopen_resume")
    @Expose var enableAppOpenResume: Boolean = true,

    // -------- Interstitial Ads --------
    @SerializedName("inter_splash")
    @Expose var enableInterSplash: Boolean = true,

    @SerializedName("inter_splash_2f")
    @Expose var enableInterSplash2F: Boolean = true,

    @SerializedName("inter_switch")
    @Expose var enableInterSwitch: Boolean = true,

    // -------- Native Ads --------
    @SerializedName("native_fullscreen_1_1")
    @Expose var enableNativeFullscreen11: Boolean = true,

    @SerializedName("native_full_screen_1_2")
    @Expose var enableNativeFullscreen12: Boolean = true,

    @SerializedName("native_language_1_1")
    @Expose var enableNativeLanguage11: Boolean = true,

    @SerializedName("native_onboarding")
    @Expose var enableNativeOnboarding: Boolean = true,

    @SerializedName("native_permission")
    @Expose var enableNativePermission: Boolean = true,

    @SerializedName("native_home")
    @Expose var enableNativeHome: Boolean = true,

    @SerializedName("native_language_2_1")
    @Expose var enableNativeLanguage21: Boolean = true,

    // -------- Reward Ads --------
    @SerializedName("reward_home")
    @Expose var enableRewardHome: Boolean = true,

    // -------- Banner Ads --------
    @SerializedName("banner_splash")
    @Expose var enableBannerSplash: Boolean = true,

    // -------- Extra fields --------
    @SerializedName("new_version")
    @Expose var newVersionRemote: String = "1.0",

    @SerializedName("check_force_update")
    @Expose var enableForceUpdate: Boolean = false,
)
//{
//    "inter_splash": true,
//    "inter_splash_2f": true,
//    "banner_splash": true,
//    "inter_switch": true,
//    "native_fullscreen_1_1": true,
//    "native_full_screen_1_2": true,
//    "native_language_1_1": true,
//    "native_onboarding": true,
//    "native_permission": true,
//    "native_home": true,
//    "native_language_2_1": true,
//    "reward_home": true,
//    "new_version": "1.1",
//    "check_force_update": false,
//}
