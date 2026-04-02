package com.example.myapplication.libads.helper

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AdConfig(
    @SerializedName("appopen_resume")
    @Expose var appopen_resume: AdUnitConfig = AdUnitConfig(),

    @SerializedName("inter_splash")
    @Expose var inter_splash: AdUnitConfig = AdUnitConfig(),

    @SerializedName("inter_splash_2f")
    @Expose var inter_splash_2f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("inter_home")
    @Expose var inter_home: AdUnitConfig = AdUnitConfig(),

    @SerializedName("inter_home_2f")
    @Expose var inter_home_2f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("inter_back")
    @Expose var inter_back: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_splash")
    @Expose var native_fs_splash: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_splash_2f")
    @Expose var native_fs_splash_2f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_splash_2")
    @Expose var native_fs_splash_2: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_splash_2_2f")
    @Expose var native_fs_splash_2_2f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_language_1_1")
    @Expose var native_language_1_1: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_language_1_2")
    @Expose var native_language_1_2: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_language_2_1")
    @Expose var native_language_2_1: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_language_2_2")
    @Expose var native_language_2_2: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_home")
    @Expose var native_home: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_feature")
    @Expose var native_feature: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_1_1")
    @Expose var native_fs_1_1: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_1_1f")
    @Expose var native_fs_1_1f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_1_2")
    @Expose var native_fs_1_2: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_1_2f")
    @Expose var native_fs_1_2f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_2_1")
    @Expose var native_fs_2_1: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_2_1f")
    @Expose var native_fs_2_1f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_2_2")
    @Expose var native_fs_2_2: AdUnitConfig = AdUnitConfig(),

    @SerializedName("native_fs_2_2f")
    @Expose var native_fs_2_2f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("reward_feature")
    @Expose var reward_feature: AdUnitConfig = AdUnitConfig(),

    @SerializedName("reward_feature_2f")
    @Expose var reward_feature_2f: AdUnitConfig = AdUnitConfig(),

    @SerializedName("banner_splash")
    @Expose var banner_splash: AdUnitConfig = AdUnitConfig(),
)

