package com.example.myapplication.ui.ui

import com.afproject.iap.IapFactory
import com.afproject.iap.listener.PurchaseServiceListener
import com.afproject.iap.model.IapProductPrice
import com.afproject.iap.model.IapPurchaseInfo
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityRewardBinding
import com.example.myapplication.utils.Constant

/**
 * Màn mua gói — chỉ dùng API module `:iap` ([IapFactory]).
 * Trạng thái PRO được đồng bộ tập trung trong [com.example.myapplication.billing.IapAppIntegration].
 */
class RewardActivity : BaseActivity<ActivityRewardBinding>() {

    override fun provideViewBinding(): ActivityRewardBinding {
        return ActivityRewardBinding.inflate(layoutInflater)
    }

    private val rewardPurchaseListener = object : PurchaseServiceListener {
        override fun onPricesUpdated(iapKeyPrices: Map<String, List<IapProductPrice>>) {}

        override fun onProductPurchased(purchaseInfo: IapPurchaseInfo?) {
            showToast("Purchase successful! Thank you for your support.")
            finish()
        }

        override fun onProductRestored(purchaseInfo: IapPurchaseInfo?) {
            // Khôi phục đã xử lý PRO trong IapAppIntegration; không finish để tránh đóng nhầm.
        }

        override fun onPurchaseFailed(purchaseInfo: IapPurchaseInfo?, billingResponseCode: Int?) {}
    }

    override fun initViews() {
        super.initViews()
        IapFactory.getInstance().registerPurchaseServiceListener(rewardPurchaseListener)

        viewBinding.apply {
            btnBuyYear.setOnClickListener {
                IapFactory.getInstance().buySubscription(this@RewardActivity, Constant.PRODUCT_ID_YEAR)
            }
            btnBuyWeek.setOnClickListener {
                IapFactory.getInstance().buySubscription(this@RewardActivity, Constant.PRODUCT_ID_WEEK)
            }
        }
    }

    override fun onDestroy() {
        IapFactory.getInstance().unregisterPurchaseServiceListener(rewardPurchaseListener)
        super.onDestroy()
    }
}
