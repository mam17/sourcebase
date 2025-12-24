package com.example.myapplication.libads.interfaces

interface InterAdsCallback {
    fun onLoadSuccess()
    fun onLoadFailed(error: String)

    fun onShowSuccess()
    fun onShowFailed(error: String)
    fun onAdClosed()
}
