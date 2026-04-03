package com.afproject.iap.debug

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.afproject.iap.IapType
import com.afproject.iap.R
import com.afproject.iap.databinding.IapDebugDialogBinding
import com.afproject.iap.listener.PurchaseListener
import com.afproject.iap.model.IapPurchaseInfo
import com.android.billingclient.api.ProductDetails

/**
 * Dialog giả lập mua khi [com.afproject.iap.IapFactory] được khởi tạo với debugMode = true.
 */
internal class IapDebugDialog(
    context: Context,
    private val typeIap: String,
    private val productDetails: ProductDetails,
    private val purchaseListener: PurchaseListener,
) : Dialog(context, R.style.IapDialogFullScreenLight) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
        )
        val wlp = window?.attributes
        wlp?.gravity = Gravity.BOTTOM
        window?.attributes = wlp
        val binding = IapDebugDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.txtTitle.text = productDetails.title
        binding.txtDescription.text = productDetails.description
        binding.txtId.text = productDetails.productId
        if (typeIap == IapType.PURCHASE) {
            binding.txtPrice.text = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
        } else {
            val phase = productDetails.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
            binding.txtPrice.text = phase?.formattedPrice
        }
        binding.txtContinuePurchase.setOnClickListener { _: View? ->
            dismiss()
            purchaseListener.onProductPurchased(
                IapPurchaseInfo(
                    purchaseState = 0,
                    purchaseTime = 2L,
                    purchaseToken = "debug-token",
                    packageName = context.packageName,
                    isAcknowledged = false,
                    isAutoRenewing = false,
                    orderId = "debug",
                    originalJson = "",
                    signature = "",
                    sku = productDetails.productId,
                    accountIdentifiers = null,
                ),
            )
        }
    }

    private companion object {
        private const val TAG = "IapDebugDialog"
    }
}
