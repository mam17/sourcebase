package com.example.myapplication.libads.helper

import android.app.Activity
import android.util.Log
import com.example.myapplication.libads.interfaces.UmpCallback
import com.google.android.ump.*

class UmpHelper(private val activity: Activity) {

    companion object {
        private const val TAG = "TAG_UMP"
    }

    private val consentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    fun requestConsent(callback: UmpCallback) {

        // ===== DEBUG SETTINGS =====
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(
                ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
            )
            .addTestDeviceHashedId(
                "B3EEABB8EE11C2BE770B684D95219ECB"
            )
            .build()

        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(debugSettings) // <<<<<< QUAN TRỌNG
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.i(TAG, "Consent info updated")
                Log.i(TAG, "Consent status: ${consentInformation.consentStatus}")
                Log.i(TAG, "Can request ads: ${consentInformation.canRequestAds()}")

                if (consentInformation.isConsentFormAvailable) {
                    loadAndShowForm(callback)
                } else {
                    callback.onConsentDone(consentInformation.canRequestAds())
                }
            },
            { error ->
                Log.e(TAG, "Consent error: ${error.message}")
                callback.onConsentDone(consentInformation.canRequestAds())
            }
        )
    }

    private fun loadAndShowForm(callback: UmpCallback) {
        UserMessagingPlatform.loadConsentForm(
            activity,
            { consentForm ->
                Log.i(TAG, "Consent form loaded")

                if (consentInformation.consentStatus ==
                    ConsentInformation.ConsentStatus.REQUIRED
                ) {
                    consentForm.show(activity) {
                        Log.i(TAG, "Consent form dismissed")
                        Log.i(TAG, "Final consent: ${consentInformation.consentStatus}")
                        callback.onConsentDone(consentInformation.canRequestAds())
                    }
                } else {
                    callback.onConsentDone(consentInformation.canRequestAds())
                }
            },
            { error ->
                Log.e(TAG, "Load form error: ${error.message}")
                callback.onConsentDone(consentInformation.canRequestAds())
            }
        )
    }
}
