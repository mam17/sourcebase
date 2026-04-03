package com.example.myapplication.billing

import com.afproject.iap.IapFactory
import com.afproject.iap.listener.PurchaseServiceListener
import com.afproject.iap.model.IapProductPrice
import com.afproject.iap.model.IapPurchaseInfo
import android.app.Application
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.SpManager

/**
 * Tích hợp chuẩn module `:iap`: listener toàn cục đồng bộ trạng thái PRO trong [SpManager]
 * khi mua / khôi phục giao dịch (khớp SKU trong [Constant]).
 *
 * Gọi [registerProSync] một lần từ [com.example.myapplication.App] ngay sau [com.afproject.iap.IapFactory.initialize].
 */
object IapAppIntegration {

    private val proSkuIds: Set<String> = setOf(
        Constant.PRODUCT_ID_REMOVE_ADS,
        Constant.PRODUCT_ID_YEAR,
        Constant.PRODUCT_ID_WEEK,
        Constant.PRODUCT_REMOVE_ADS,
    )

    @Volatile
    private var registered = false

    fun registerProSync(application: Application) {
        if (registered) return
        synchronized(this) {
            if (registered) return
            registered = true
            val spManager = SpManager.getInstance(application)
            IapFactory.getInstance().registerPurchaseServiceListener(
                object : PurchaseServiceListener {
                    override fun onPricesUpdated(iapKeyPrices: Map<String, List<IapProductPrice>>) {}

                    override fun onProductPurchased(purchaseInfo: IapPurchaseInfo?) {
                        if (purchaseInfo?.sku in proSkuIds) {
                            spManager.setPro(true)
                        }
                    }

                    override fun onProductRestored(purchaseInfo: IapPurchaseInfo?) {
                        if (purchaseInfo?.sku in proSkuIds) {
                            spManager.setPro(true)
                        }
                    }

                    override fun onQueryFinished() {
                        val owned = IapFactory.getInstance().getProductPurchaseList()
                        val hasPro = owned.any { it.sku in proSkuIds }
                        spManager.setPro(hasPro)
                    }

                    override fun onPurchaseFailed(purchaseInfo: IapPurchaseInfo?, billingResponseCode: Int?) {}
                },
            )
        }
    }
}
