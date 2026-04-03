package com.afproject.iap.listener

import com.afproject.iap.model.IapProductPrice
import com.afproject.iap.model.IapPurchaseInfo

interface PurchaseServiceListener : BillingServiceListener {
    override fun onPricesUpdated(iapKeyPrices: Map<String, List<IapProductPrice>>)

    fun onProductPurchased(purchaseInfo: IapPurchaseInfo?)

    fun onProductRestored(purchaseInfo: IapPurchaseInfo?)

    fun onQueryFinished() {}
}
