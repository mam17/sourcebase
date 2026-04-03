package com.afproject.iap

import android.app.Activity
import android.app.Application
import android.util.Log
import com.afproject.iap.debug.IapDebugDialog
import com.afproject.iap.listener.BillingClientConnectionListener
import com.afproject.iap.listener.PurchaseListener
import com.afproject.iap.listener.PurchaseServiceListener
import com.afproject.iap.model.IapProductPrice
import com.afproject.iap.model.IapPurchaseInfo
import com.afproject.iap.security.Security
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

internal class IapFactoryImpl(
    application: Application,
    private val iapList: List<IapItem>,
    private val debugMode: Boolean = false,
) : IapFactory,
    PurchasesUpdatedListener,
    AcknowledgePurchaseResponseListener {

    private var billingClient: BillingClient
    private val billingClientConnectionCallback = CopyOnWriteArrayList<BillingClientConnectionListener>()
    private val purchaseServiceListeners = CopyOnWriteArrayList<PurchaseServiceListener>()
    private val productDetailsMap = ConcurrentHashMap<String, ProductDetails>()
    private var decodedKey: String? = null
    private val listIapOwned = CopyOnWriteArrayList<IapPurchaseInfo>()
    private var isConsumable = false
    private val isQueryFinished = AtomicBoolean(false)

    init {
        val contextLocal = application.applicationContext ?: application
        billingClient =
            BillingClient.newBuilder(contextLocal)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected. Retrying...")
                // In production, might want exponential backoff
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(TAG, "onBillingSetupFinished: $billingResult")
                if (billingResult.isOk()) {
                    invokeBillingClientConnectionListener {
                        it.onConnected(true, billingResult.responseCode)
                    }
                    syncProductDetailsAndQueryPurchases()
                } else {
                    invokeBillingClientConnectionListener {
                        it.onConnected(false, billingResult.responseCode)
                    }
                }
            }
        })
    }

    private fun syncProductDetailsAndQueryPurchases() {
        val inAppItems = iapList.filter { it.type == BillingClient.ProductType.INAPP }
        val subsItems = iapList.filter { it.type == BillingClient.ProductType.SUBS }

        inAppItems.syncPurchaseItemsToListProduct {
            subsItems.syncPurchaseItemsToListProduct {
                CoroutineScope(Dispatchers.IO).launch {
                    queryPurchases()
                }
            }
        }
    }

    private fun BillingResult.isOk(): Boolean =
        this.responseCode == BillingClient.BillingResponseCode.OK

    private fun List<IapItem>.syncPurchaseItemsToListProduct(onDone: () -> Unit) {
        if (!billingClient.isReady) {
            Log.d(TAG, "syncPurchaseItemsToListProduct: billing not ready")
            onDone()
            return
        }
        if (this.isEmpty()) {
            onDone()
            return
        }
        val productList = this.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it.itemId)
                .setProductType(it.type)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            if (billingResult.isOk()) {
                productDetailsList.productDetailsList.forEach { pd ->
                    productDetailsMap[pd.productId] = pd
                }
                val priceMap = productDetailsMap.mapValues { (_, pd) ->
                    pd.toIapProductPrices()
                }
                dispatchPricesUpdated(priceMap)
            }
            onDone()
        }
    }

    private fun ProductDetails.toIapProductPrices(): List<IapProductPrice> {
        return when (this.productType) {
            BillingClient.ProductType.SUBS -> {
                this.subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.map { phase ->
                    IapProductPrice(
                        title = this.title,
                        description = this.description,
                        priceCurrencyCode = phase.priceCurrencyCode,
                        price = phase.formattedPrice,
                        priceAmount = phase.priceAmountMicros.toDouble() / 1_000_000.0,
                        billingCycleCount = phase.billingCycleCount,
                        billingPeriod = phase.billingPeriod,
                        recurrenceMode = phase.recurrenceMode,
                    )
                } ?: emptyList()
            }
            else -> {
                listOf(
                    IapProductPrice(
                        title = this.title,
                        description = this.description,
                        priceCurrencyCode = this.oneTimePurchaseOfferDetails?.priceCurrencyCode,
                        price = this.oneTimePurchaseOfferDetails?.formattedPrice,
                        priceAmount = this.oneTimePurchaseOfferDetails?.priceAmountMicros?.toDouble()?.div(1_000_000.0),
                        billingCycleCount = null,
                        billingPeriod = null,
                        recurrenceMode = ProductDetails.RecurrenceMode.NON_RECURRING,
                    )
                )
            }
        }
    }

    private fun dispatchPricesUpdated(map: Map<String, List<IapProductPrice>>) {
        if (map.isEmpty()) return
        invokePurchaseServiceListener { it.onPricesUpdated(map) }
    }

    private fun launchBillingFlow(
        activity: Activity,
        sku: String,
        type: String,
        obfuscatedAccountId: String?,
        obfuscatedProfileId: String?,
    ) {
        sku.toProductDetails(type) { details ->
            if (details == null) {
                log("launchBillingFlow: product details not found for $sku")
                return@toProductDetails
            }

            if (debugMode) {
                activity.runOnUiThread {
                    IapDebugDialog(
                        activity,
                        type,
                        details,
                        object : PurchaseListener {
                            override fun onProductPurchased(purchaseInfo: IapPurchaseInfo) {
                                invokePurchaseServiceListener { it.onProductPurchased(purchaseInfo) }
                                if (type == BillingClient.ProductType.SUBS) {
                                    subscriptionOwned(purchaseInfo, false)
                                } else {
                                    productOwned(purchaseInfo, false)
                                }
                            }
                            override fun displayErrorMessage(errorMsg: String?) {
                                invokePurchaseServiceListener { it.onPurchaseFailed(null, null) }
                            }
                            override fun onUserCancelBilling() {
                                invokePurchaseServiceListener { it.onPurchaseFailed(null, null) }
                            }
                        }
                    ).show()
                }
            } else {
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .apply {
                            if (type == BillingClient.ProductType.SUBS) {
                                details.subscriptionOfferDetails?.getOrNull(0)?.let {
                                    setOfferToken(it.offerToken)
                                }
                            }
                        }
                        .build()
                )
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .apply {
                        obfuscatedAccountId?.let { setObfuscatedAccountId(it) }
                        obfuscatedProfileId?.let { setObfuscatedProfileId(it) }
                    }
                    .build()
                
                val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                Log.d(TAG, "launchBillingFlow: $billingResult")
            }
        }
    }

    private fun String.toProductDetails(
        type: String,
        done: (ProductDetails?) -> Unit
    ) {
        if (!billingClient.isReady) {
            done(null)
            return
        }
        val cached = productDetailsMap[this]
        if (cached != null) {
            done(cached)
            return
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(this)
                    .setProductType(type)
                    .build()
            ))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, list ->
            if (billingResult.isOk()) {
                val pd = list.productDetailsList.find { it.productId == this }
                if (pd != null) productDetailsMap[pd.productId] = pd
                done(pd)
            } else {
                done(null)
            }
        }
    }

    override fun buySubscription(activity: Activity, subId: String) {
        if (!subId.isProductReady()) {
            log("buySubscription: SKU not ready: $subId")
            return
        }
        launchBillingFlow(activity, subId, BillingClient.ProductType.SUBS, null, null)
    }

    override fun buyIap(activity: Activity, iapId: String, isConsumable: Boolean) {
        this.isConsumable = isConsumable
        if (!iapId.isProductReady()) {
            log("buyIap: SKU not ready: $iapId")
            return
        }
        launchBillingFlow(activity, iapId, BillingClient.ProductType.INAPP, null, null)
    }

    override fun getPriceById(skuId: String, type: String): String? {
        val pd = productDetailsMap[skuId] ?: return null
        return if (type == BillingClient.ProductType.INAPP) {
            pd.oneTimePurchaseOfferDetails?.formattedPrice
        } else {
            pd.subscriptionOfferDetails?.lastOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
        }
    }

    override fun getOfferById(skuId: String): String? {
        val pd = productDetailsMap[skuId] ?: return null
        var offerPrice: String? = null
        pd.subscriptionOfferDetails?.forEach { offer ->
            offer.pricingPhases.pricingPhaseList.find { it.recurrenceMode == 2 }?.let {
                offerPrice = it.formattedPrice
            }
        }
        return offerPrice
    }

    override fun getPriceByNumOfWeek(skuId: String, type: String, numWeek: Int): String {
        val pd = productDetailsMap[skuId]
        val price = if (type == BillingClient.ProductType.INAPP) {
            pd?.oneTimePurchaseOfferDetails?.priceAmountMicros ?: 0L
        } else {
            pd?.subscriptionOfferDetails?.lastOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros ?: 0L
        }
        val currency = if (type == BillingClient.ProductType.INAPP) {
            pd?.oneTimePurchaseOfferDetails?.priceCurrencyCode ?: "USD"
        } else {
            pd?.subscriptionOfferDetails?.lastOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceCurrencyCode ?: "USD"
        }
        return IapPriceFormatter.formatPerWeek(price, currency, numWeek)
    }

    override fun registerBillingClientConnectionListener(adCallback: BillingClientConnectionListener) {
        billingClientConnectionCallback.add(adCallback)
    }

    override fun unregisterAllBillingClientConnectionListener() {
        billingClientConnectionCallback.clear()
    }

    override fun unregisterBillingClientConnectionListener(adCallback: BillingClientConnectionListener) {
        billingClientConnectionCallback.remove(adCallback)
    }

    private fun invokeBillingClientConnectionListener(action: (BillingClientConnectionListener) -> Unit) {
        billingClientConnectionCallback.forEach(action)
    }

    override fun registerPurchaseServiceListener(adCallback: PurchaseServiceListener) {
        purchaseServiceListeners.add(adCallback)
    }

    override fun unregisterPurchaseServiceListener(adCallback: PurchaseServiceListener) {
        purchaseServiceListeners.remove(adCallback)
    }

    override fun unregisterAllPurchaseServiceListener() {
        purchaseServiceListeners.clear()
    }

    private fun invokePurchaseServiceListener(action: (PurchaseServiceListener) -> Unit) {
        purchaseServiceListeners.forEach(action)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        val responseCode = billingResult.responseCode
        Log.d(TAG, "onPurchasesUpdated: code=$responseCode msg=${billingResult.debugMessage}")
        
        if (billingResult.isOk() && purchases != null) {
            processPurchases(purchases)
        } else {
            when (responseCode) {
                BillingClient.BillingResponseCode.USER_CANCELED -> log("onPurchasesUpdated: user canceled")
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    log("onPurchasesUpdated: already owned")
                    CoroutineScope(Dispatchers.IO).launch { queryPurchases() }
                }
                else -> updateFailedPurchases(purchases?.flatMap { p -> p.products.map { getPurchaseInfo(p, it) } }, responseCode)
            }
        }
    }

    private fun processPurchases(purchasesList: List<Purchase>, isRestore: Boolean = false) {
        Log.d(TAG, "processPurchases: size=${purchasesList.size} isRestore=$isRestore")
        for (purchase in purchasesList) {
            val isPending = purchase.purchaseState == Purchase.PurchaseState.PENDING
            val isPurchased = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            
            if (isPending || isPurchased) {
                if (!isSignatureValid(purchase)) {
                    log("processPurchases: invalid signature for ${purchase.orderId}")
                    continue
                }

                purchase.products.forEach { productId ->
                    val purchaseInfo = getPurchaseInfo(purchase, productId)
                    val pd = productDetailsMap[productId]
                    
                    if (isPurchased) {
                        if (pd?.productType == BillingClient.ProductType.INAPP && isConsumable) {
                            tryConsume(purchase, purchaseInfo)
                        } else {
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase, purchaseInfo)
                            } else {
                                // Already acknowledged or just restored
                                if (pd?.productType == BillingClient.ProductType.SUBS) {
                                    subscriptionOwned(purchaseInfo, isRestore)
                                } else {
                                    productOwned(purchaseInfo, isRestore)
                                }
                            }
                        }
                    } else {
                        // Pending
                        invokePurchaseServiceListener { it.onProductPurchased(purchaseInfo) }
                    }
                }
            }
        }
    }

    private fun tryConsume(purchase: Purchase, purchaseInfo: IapPurchaseInfo) {
        val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.consumeAsync(consumeParams) { result, _ ->
            if (result.isOk()) {
                productOwned(purchaseInfo, false)
            } else {
                updateFailedPurchase(purchaseInfo, result.responseCode)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase, purchaseInfo: IapPurchaseInfo) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        billingClient.acknowledgePurchase(params) { result ->
            if (result.isOk()) {
                val pd = productDetailsMap[purchaseInfo.sku]
                if (pd?.productType == BillingClient.ProductType.SUBS) {
                    subscriptionOwned(purchaseInfo, false)
                } else {
                    productOwned(purchaseInfo, false)
                }
            } else {
                updateFailedPurchase(purchaseInfo, result.responseCode)
            }
        }
    }

    override fun isProductPurchased(): Boolean = listIapOwned.isNotEmpty()

    override fun getProductPurchaseList(): List<IapPurchaseInfo> = listIapOwned.toList()

    override fun isBillingClientReady(): Boolean = billingClient.isReady
    
    override fun isQueryFinished(): Boolean = isQueryFinished.get()

    private fun subscriptionOwned(purchaseInfo: IapPurchaseInfo, isRestore: Boolean) {
        addOwnedProduct(purchaseInfo)
        invokePurchaseServiceListener { 
            if (isRestore) it.onProductRestored(purchaseInfo) else it.onProductPurchased(purchaseInfo)
        }
    }

    private fun productOwned(purchaseInfo: IapPurchaseInfo, isRestore: Boolean) {
        addOwnedProduct(purchaseInfo)
        invokePurchaseServiceListener { 
            if (isRestore) it.onProductRestored(purchaseInfo) else it.onProductPurchased(purchaseInfo)
        }
    }

    private fun addOwnedProduct(info: IapPurchaseInfo) {
        if (listIapOwned.none { it.purchaseToken == info.purchaseToken && it.sku == info.sku }) {
            listIapOwned.add(info)
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        val key = decodedKey ?: return true
        return Security.verifyPurchase(key, purchase.originalJson, purchase.signature)
    }

    private fun getPurchaseInfo(purchase: Purchase, productId: String): IapPurchaseInfo {
        return IapPurchaseInfo(
            purchaseState = purchase.purchaseState,
            isAcknowledged = purchase.isAcknowledged,
            isAutoRenewing = purchase.isAutoRenewing,
            orderId = purchase.orderId,
            originalJson = purchase.originalJson,
            packageName = purchase.packageName,
            purchaseTime = purchase.purchaseTime,
            purchaseToken = purchase.purchaseToken,
            signature = purchase.signature,
            sku = productId,
            accountIdentifiers = purchase.accountIdentifiers,
        )
    }

    private suspend fun queryPurchases() {
        listIapOwned.clear()
        
        val inAppResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        )
        if (inAppResult.billingResult.isOk()) {
            processPurchases(inAppResult.purchasesList, isRestore = true)
        }

        val subsResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        if (subsResult.billingResult.isOk()) {
            processPurchases(subsResult.purchasesList, isRestore = true)
        }
        
        isQueryFinished.set(true)
        invokePurchaseServiceListener { it.onQueryFinished() }
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    private fun String.isProductReady(): Boolean = productDetailsMap.containsKey(this)

    private fun updateFailedPurchases(purchaseInfo: List<IapPurchaseInfo>?, responseCode: Int) {
        purchaseInfo?.forEach { updateFailedPurchase(it, responseCode) }
    }

    private fun updateFailedPurchase(info: IapPurchaseInfo? = null, code: Int? = null) {
        invokePurchaseServiceListener { it.onPurchaseFailed(info, code) }
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        log("onAcknowledgePurchaseResponse: $billingResult")
        if (!billingResult.isOk()) {
            updateFailedPurchase(code = billingResult.responseCode)
        }
    }

    private companion object {
        private const val TAG = "IapFactoryImpl"
    }
}
