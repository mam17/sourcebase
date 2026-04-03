package com.afproject.iap.model

import com.android.billingclient.api.AccountIdentifiers

/**
 * Giá / mô tả hiển thị sau khi query product (không phải [com.android.billingclient.api.ProductDetails] gốc).
 */
data class IapProductPrice(
    val title: String?,
    val description: String?,
    val price: String?,
    val priceAmount: Double?,
    val priceCurrencyCode: String?,
    val billingCycleCount: Int?,
    val billingPeriod: String?,
    val recurrenceMode: Int?,
)

/**
 * Snapshot thông tin giao dịch (copy từ Purchase + metadata cần cho app).
 */
data class IapPurchaseInfo(
    val purchaseState: Int,
    val isAcknowledged: Boolean,
    val isAutoRenewing: Boolean,
    val orderId: String?,
    val originalJson: String,
    val packageName: String,
    val purchaseTime: Long,
    val purchaseToken: String,
    val signature: String,
    val sku: String,
    val accountIdentifiers: AccountIdentifiers?,
)
