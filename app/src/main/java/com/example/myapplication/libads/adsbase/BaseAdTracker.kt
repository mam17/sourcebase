package com.example.myapplication.libads.adsbase

abstract class BaseAdTracker {

    private var isPaidLogged = false
    private var isImpressionLogged = false

    protected fun logPaidOnce(
        action: () -> Unit
    ) {
        if (isPaidLogged) return
        isPaidLogged = true
        action()
    }

    protected fun logImpressionOnce(
        action: () -> Unit
    ) {
        if (isImpressionLogged) return
        isImpressionLogged = true
        action()
    }

    fun resetTracker() {
        isPaidLogged = false
        isImpressionLogged = false
    }

}
