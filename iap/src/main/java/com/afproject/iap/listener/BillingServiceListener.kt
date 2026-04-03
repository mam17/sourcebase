package com.afproject.iap.listener

import com.afproject.iap.model.IapProductPrice
import com.afproject.iap.model.IapPurchaseInfo

interface BillingServiceListener {
    fun onPricesUpdated(iapKeyPrices: Map<String, List<IapProductPrice>>)
    fun onPurchaseFailed(purchaseInfo: IapPurchaseInfo?, billingResponseCode: Int?)
}
