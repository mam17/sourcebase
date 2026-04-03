package com.afproject.iap.listener

fun interface BillingClientConnectionListener {
    fun onConnected(status: Boolean, billingResponseCode: Int)
}
