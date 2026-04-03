package com.afproject.iap

import android.app.Activity
import android.app.Application
import com.afproject.iap.listener.BillingClientConnectionListener
import com.afproject.iap.listener.PurchaseServiceListener
import com.afproject.iap.model.IapPurchaseInfo
import com.android.billingclient.api.BillingClient

/**
 * Facade chính: gọi [initialize] một lần trong [Application], sau đó dùng [getInstance].
 *
 * Tích hợp dự án khác: copy cả thư mục `iap/`, thêm `include(":iap")` trong `settings`,
 * khai báo plugin `com.android.library` (cùng phiên bản AGP với app), thêm
 * `implementation(project(":iap"))` vào app. Có thể đổi `namespace` trong `iap/build.gradle.kts`
 * và đổi package `com.afproject.iap` bằng Refactor trong IDE.
 */
interface IapFactory {
    fun buySubscription(activity: Activity, subId: String)
    fun buyIap(activity: Activity, iapId: String, isConsumable: Boolean = false)
    fun getPriceById(skuId: String, type: String): String?
    fun getOfferById(skuId: String): String?
    fun getPriceByNumOfWeek(skuId: String, type: String, numWeek: Int): String

    fun registerBillingClientConnectionListener(adCallback: BillingClientConnectionListener)
    fun unregisterBillingClientConnectionListener(adCallback: BillingClientConnectionListener)
    fun unregisterAllBillingClientConnectionListener()

    fun registerPurchaseServiceListener(adCallback: PurchaseServiceListener)
    fun unregisterPurchaseServiceListener(adCallback: PurchaseServiceListener)
    fun unregisterAllPurchaseServiceListener()

    fun isProductPurchased(): Boolean
    fun getProductPurchaseList(): List<IapPurchaseInfo>

    /** `true` khi [com.android.billingclient.api.BillingClient] đã sẵn sàng (đồng bộ SKU / query). */
    fun isBillingClientReady(): Boolean

    /** `true` khi lần query đầu tiên (INAPP / SUBS) đã hoàn thành. */
    fun isQueryFinished(): Boolean

    companion object {
        @Volatile
        private var INSTANCE: IapFactory? = null

        fun initialize(application: Application, iapList: List<IapItem>, debugMode: Boolean = false): IapFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: IapFactoryImpl(application, iapList, debugMode).also { INSTANCE = it }
            }
        }

        fun getInstance(): IapFactory {
            return INSTANCE
                ?: error("IapFactory is not initialized. Call initialize() first.")
        }
    }
}

annotation class IapType {
    companion object {
        const val PURCHASE = BillingClient.ProductType.INAPP
        const val SUBSCRIPTION = BillingClient.ProductType.SUBS
    }
}
