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

class GoogleMobileAdsConsentManager private constructor(context: Context) {

    val consentInformation: ConsentInformation =
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

    interface OnConsentGatheringCompleteListener {
        fun onConsentGatheringComplete(formError: FormError?)
    }

    fun canRequestAds(): Boolean = consentInformation.canRequestAds()

    fun isPrivacyOptionsRequired(): Boolean {
        return consentInformation.privacyOptionsRequirementStatus ==
                PrivacyOptionsRequirementStatus.REQUIRED
    }

    fun gatherConsent(
        activity: Activity,
        testDeviceIds: List<String> = emptyList(),
        onComplete: OnConsentGatheringCompleteListener
    ) {
        consentInformation.reset()

        val debugSettings = ConsentDebugSettings.Builder(activity).apply {
            testDeviceIds.forEach { addTestDeviceHashedId(it) }
            setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
        }.build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    onComplete.onConsentGatheringComplete(formError)
                }
            },
            { formError ->
                onComplete.onConsentGatheringComplete(formError)
            }
        )
    }


    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: OnConsentFormDismissedListener
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onDismissed)
    }
}
