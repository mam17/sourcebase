package com.example.myapplication.domain.layer

import com.example.myapplication.libads.admods.NativeAds

data class OnboardingModel(
    val resImage: Int, val resTitle: Int, val resDescription: Int, val nativeFull: NativeAds? = null
) {
    companion object {
        const val FULL_NATIVE_FLAG = 1822
    }
}