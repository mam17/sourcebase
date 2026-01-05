package com.example.myapplication.libads.adsbase

import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseAdTracker {

    private val isPaidLogged = AtomicBoolean(false)
    private val isImpressionLogged = AtomicBoolean(false)

    protected fun logPaidOnce(action: () -> Unit) {
        if (isPaidLogged.compareAndSet(false, true)) {
            action()
        }
    }

    protected fun logImpressionOnce(action: () -> Unit) {
        if (isImpressionLogged.compareAndSet(false, true)) {
            action()
        }
    }

    open fun resetTracker() {
        isPaidLogged.set(false)
        isImpressionLogged.set(false)
    }

}
