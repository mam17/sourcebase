package com.example.myapplication.libads.consent

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm.OnConsentFormDismissedListener
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Quản lý xin quyền Consent cho tất cả định dạng quảng cáo (Banner, Native, Interstitial, Rewarded...).
 * Gọi trong Application hoặc Activity đầu tiên để đảm bảo tuân thủ GDPR.
 */
class GoogleMobileAdsConsentManager private constructor(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)

    companion object {
        @Volatile
        private var instance: GoogleMobileAdsConsentManager? = null

        fun getInstance(context: Context): GoogleMobileAdsConsentManager {
            return instance ?: synchronized(this) {
                instance ?: GoogleMobileAdsConsentManager(context).also { instance = it }
            }
        }
    }

    /** Callback khi hoàn tất việc gather consent */
    interface OnConsentGatheringCompleteListener {
        fun onConsentGatheringComplete(formError: FormError?)
    }

    /** Kiểm tra xem đã được phép request quảng cáo chưa */
    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    /** Kiểm tra xem form Privacy Options có bắt buộc hiển thị không */
    fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus ==
                PrivacyOptionsRequirementStatus.REQUIRED
    }

    /**
     * Gọi trong Activity khởi động (vd: SplashActivity hoặc MainActivity).
     * SDK sẽ tự động hiển thị form consent nếu cần.
     */
    fun gatherConsent(
        activity: Activity,
        testDeviceIds: List<String> = emptyList(),
        onComplete: OnConsentGatheringCompleteListener
    ) {
        val debugSettings = ConsentDebugSettings.Builder(activity).apply {
            testDeviceIds.forEach { addTestDeviceHashedId(it) }
            // Uncomment dòng dưới nếu muốn test ở EU:
            // setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
        }.build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Khi update thành công, nếu cần sẽ hiển thị form consent.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { formError ->
                    onComplete.onConsentGatheringComplete(formError)
                }
            },
            { formError ->
                // Khi có lỗi update thông tin consent
                onComplete.onConsentGatheringComplete(formError)
            }
        )
    }

    /** Hiển thị lại form Privacy Options (nếu user muốn thay đổi lựa chọn). */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onDismissed)
    }
}
