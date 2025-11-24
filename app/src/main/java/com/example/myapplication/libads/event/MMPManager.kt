package com.example.myapplication.libads.event

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.adjust.sdk.*
import com.example.myapplication.BuildConfig
import java.util.*

object MMPManager {

    private const val TAG = "MMPManager"
    private const val PREFS_NAME = "mmp_preferences"
    private const val PREF_FIRST_LAUNCH = "first_launch"
    private const val PREF_USER_ID = "user_id"

    // Event Tokens - Update these with your actual tokens from Adjust dashboard
    object EventTokens {
        const val REGISTRATION = "your_registration_event_token"
        const val LOGIN = "your_login_event_token"
        const val PURCHASE = "your_purchase_event_token"
        const val SUBSCRIPTION = "your_subscription_event_token"
    }

    // Configuration
    private var isInitialized = false
    private lateinit var appContext: Context

    /**
     * Initialize MMP Manager
     */
    fun initialize(application: Application, appToken: String, isDebug: Boolean = BuildConfig.DEBUG) {
        if (isInitialized) {
            Log.w(TAG, "MMPManager already initialized")
            return
        }

        this.appContext = application.applicationContext

        val environment = if (isDebug) {
            AdjustConfig.ENVIRONMENT_SANDBOX
        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }

        val config = AdjustConfig(appContext, appToken, environment)

        // Basic configuration - SỬ DỤNG METHODS THAY VÌ PROPERTIES
        config.setLogLevel(if (isDebug) LogLevel.VERBOSE else LogLevel.SUPPRESS)
        config.enableSendingInBackground()

        // Event tracking callbacks
        config.setOnEventTrackingSucceededListener { eventSuccess ->
            Log.d(TAG, "✅ Event tracked successfully: ${eventSuccess.eventToken}")
            Log.d(TAG, "Callback parameters: ${eventSuccess.callbackId}")
        }

        config.setOnEventTrackingFailedListener { eventFailure ->
            Log.e(TAG, "❌ Event tracking failed: ${eventFailure.eventToken}")
            Log.e(TAG, "Will retry: ${eventFailure.willRetry}")
        }

        // Session tracking callbacks
        config.setOnSessionTrackingSucceededListener { sessionSuccess ->
            Log.d(TAG, "✅ Session tracked successfully")
        }

        config.setOnSessionTrackingFailedListener { sessionFailure ->
            Log.e(TAG, "❌ Session tracking failed")
        }

        // Initialize Adjust SDK
        Adjust.initSdk(config)

        isInitialized = true
        trackFirstLaunch()

        Log.i(TAG, "🎉 MMPManager initialized successfully!")
    }

    /**
     * Track user registration
     */
    fun trackRegistration(userId: String? = null, method: String = "email") {
        if (!isInitialized) {
            Log.w(TAG, "MMPManager not initialized. Call initialize() first.")
            return
        }

        val event = AdjustEvent(EventTokens.REGISTRATION)

        userId?.let {
            event.addCallbackParameter("user_id", it)
            event.addPartnerParameter("user_id", it)
            storeUserId(it)
        }

        event.addCallbackParameter("registration_method", method)
        event.addPartnerParameter("registration_method", method)

        Adjust.trackEvent(event)
        Log.d(TAG, "📝 Registration tracked for user: $userId")
    }

    /**
     * Track user login
     */
    fun trackLogin(userId: String? = null, method: String = "email") {
        if (!isInitialized) {
            Log.w(TAG, "MMPManager not initialized. Call initialize() first.")
            return
        }

        val event = AdjustEvent(EventTokens.LOGIN)

        userId?.let {
            event.addCallbackParameter("user_id", it)
            event.addPartnerParameter("user_id", it)
        }

        event.addCallbackParameter("login_method", method)
        event.addPartnerParameter("login_method", method)

        Adjust.trackEvent(event)
        Log.d(TAG, "🔑 Login tracked for user: $userId")
    }

    /**
     * Track purchase with revenue - QUAN TRỌNG CHO ROAS
     */
    fun trackPurchase(
        revenue: Double,
        currency: String,
        productId: String,
        transactionId: String? = null,
        orderId: String? = null,
        extraParams: Map<String, String> = emptyMap()
    ) {
        if (!isInitialized) {
            Log.w(TAG, "MMPManager not initialized. Call initialize() first.")
            return
        }

        val event = AdjustEvent(EventTokens.PURCHASE)

        // Revenue tracking - QUAN TRỌNG
        event.setRevenue(revenue, currency)

        // Transaction ID to prevent duplicates
        transactionId?.let {
            event.setOrderId(it)
        }

        // Product information
        event.addCallbackParameter("product_id", productId)
        event.addPartnerParameter("product_id", productId)

        // Additional parameters
        extraParams.forEach { (key, value) ->
            event.addCallbackParameter(key, value)
            event.addPartnerParameter(key, value)
        }

        // User ID if available
        getStoredUserId()?.let { userId ->
            event.addCallbackParameter("user_id", userId)
            event.addPartnerParameter("user_id", userId)
        }

        Adjust.trackEvent(event)
        Log.d(TAG, "💰 Purchase tracked: $productId, Revenue: $revenue $currency")
    }

    /**
     * Track subscription purchase
     */
    fun trackSubscription(
        revenue: Double,
        currency: String,
        productId: String,
        subscriptionType: String,
        transactionId: String? = null
    ) {
        if (!isInitialized) {
            Log.w(TAG, "MMPManager not initialized. Call initialize() first.")
            return
        }

        val event = AdjustEvent(EventTokens.SUBSCRIPTION)

        event.setRevenue(revenue, currency)
        transactionId?.let { event.setOrderId(it) }

        event.addCallbackParameter("product_id", productId)
        event.addPartnerParameter("product_id", productId)
        event.addCallbackParameter("subscription_type", subscriptionType)
        event.addPartnerParameter("subscription_type", subscriptionType)

        getStoredUserId()?.let { userId ->
            event.addCallbackParameter("user_id", userId)
            event.addPartnerParameter("user_id", userId)
        }

        Adjust.trackEvent(event)
        Log.d(TAG, "📅 Subscription tracked: $productId, Type: $subscriptionType")
    }

    /**
     * Track custom event
     */
    fun trackCustomEvent(
        eventToken: String,
        revenue: Double? = null,
        currency: String? = null,
        callbackParams: Map<String, String> = emptyMap(),
        partnerParams: Map<String, String> = emptyMap()
    ) {
        if (!isInitialized) {
            Log.w(TAG, "MMPManager not initialized. Call initialize() first.")
            return
        }

        val event = AdjustEvent(eventToken)

        // Add revenue if provided
        revenue?.let { rev ->
            currency?.let { curr ->
                event.setRevenue(rev, curr)
            }
        }

        // Add callback parameters
        callbackParams.forEach { (key, value) ->
            event.addCallbackParameter(key, value)
        }

        // Add partner parameters
        partnerParams.forEach { (key, value) ->
            event.addPartnerParameter(key, value)
        }

        Adjust.trackEvent(event)
        Log.d(TAG, "🎯 Custom event tracked: $eventToken")
    }

    /**
     * Handle deep link for attribution
     */
    fun handleDeepLink(deepLinkUrl: String, context: Context) {
//        if (!isInitialized) {
//            Log.w(TAG, "MMPManager not initialized. Call initialize() first.")
//            return
//        }
//
//        try {
//            val deeplink = AdjustDeeplink(deepLinkUrl, context)
//            Adjust.processDeeplink(deeplink, context)
//            Log.d(TAG, "🔗 Deep link handled: $deepLinkUrl")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling deep link: ${e.message}")
//        }
    }

    /**
     * Add global callback parameters (for all events)
     */
    fun addGlobalCallbackParameter(key: String, value: String) {
        if (isInitialized) {
            Adjust.addGlobalCallbackParameter(key, value)
        }
    }

    /**
     * Add global partner parameters (for all events)
     */
    fun addGlobalPartnerParameter(key: String, value: String) {
        if (isInitialized) {
            Adjust.addGlobalPartnerParameter(key, value)
        }
    }

    /**
     * Remove global callback parameter
     */
    fun removeGlobalCallbackParameter(key: String) {
        if (isInitialized) {
            Adjust.removeGlobalCallbackParameter(key)
        }
    }

    /**
     * Remove global partner parameter
     */
    fun removeGlobalPartnerParameter(key: String) {
        if (isInitialized) {
            Adjust.removeGlobalPartnerParameter(key)
        }
    }

    /**
     * Enable/disable tracking (for GDPR compliance)
     */
    fun setTrackingEnabled(enabled: Boolean) {
        if (isInitialized) {
            if (enabled) {
                Adjust.enable()
            } else {
                Adjust.disable()
            }
        }
    }

    /**
     * Check if tracking is enabled
     */
    fun isTrackingEnabled(context: Context, listener: OnIsEnabledListener) {
        if (isInitialized) {
            Adjust.isEnabled(context, listener)
        }
    }

    /**
     * Get Adjust device ID
     */
    fun getAdjustId(listener: OnAdidReadListener) {
        if (isInitialized) {
            Adjust.getAdid(listener)
        }
    }

    /**
     * Get attribution information
     */
    fun getAttribution(listener: OnAttributionReadListener) {
        if (isInitialized) {
            Adjust.getAttribution(listener)
        }
    }

    /**
     * GDPR compliance - Call when user wants to be forgotten
     */
    fun gdprForgetMe(context: Context) {
        if (isInitialized) {
            Adjust.gdprForgetMe(context)
        }
    }

    /**
     * Track measurement consent
     */
    fun trackMeasurementConsent(consent: Boolean) {
        if (isInitialized) {
            Adjust.trackMeasurementConsent(consent)
        }
    }

    /**
     * App lifecycle methods - QUAN TRỌNG
     */
    fun onResume() {
        if (isInitialized) {
            Adjust.onResume()
        }
    }

    fun onPause() {
        if (isInitialized) {
            Adjust.onPause()
        }
    }

    /**
     * Set push token
     */
    fun setPushToken(token: String, context: Context) {
        if (isInitialized) {
            Adjust.setPushToken(token, context)
        }
    }

    /**
     * Set referrer
     */
    fun setReferrer(referrer: String, context: Context) {
        if (isInitialized) {
            Adjust.setReferrer(referrer, context)
        }
    }

    // Private helper methods
    private fun trackFirstLaunch() {
        val prefs = getPreferences()
        if (!prefs.getBoolean(PREF_FIRST_LAUNCH, false)) {
            prefs.edit().putBoolean(PREF_FIRST_LAUNCH, true).apply()
            Log.d(TAG, "🚀 First launch tracked")
        }
    }

    private fun storeUserId(userId: String) {
        getPreferences().edit().putString(PREF_USER_ID, userId).apply()
    }

    private fun getStoredUserId(): String? {
        return getPreferences().getString(PREF_USER_ID, null)
    }

    private fun getPreferences(): SharedPreferences {
        return appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if MMPManager is initialized
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Simple test method
     */
    fun testIntegration() {
        if (!isInitialized) {
            Log.e(TAG, "❌ Not initialized")
            return
        }

        // Test event
        trackCustomEvent("test_event",
            callbackParams = mapOf("test_param" to "test_value"))

        // Test getting Adjust ID
        getAdjustId { adid ->
            Log.d(TAG, "📱 Adjust ID: $adid")
        }

        Log.d(TAG, "✅ Integration test completed")
    }
}