package com.afproject.iap.listener

import com.afproject.iap.model.IapPurchaseInfo

interface PurchaseListener {
    fun onProductPurchased(purchaseInfo: IapPurchaseInfo)
    fun displayErrorMessage(errorMsg: String?)
    fun onUserCancelBilling()
}
