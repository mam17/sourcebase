package com.example.myapplication.utils.ads.base

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.example.myapplication.ui.dialog.DialogAdsLoading
import com.example.myapplication.utils.ads.interfaces.AdLoadCallback
import com.example.myapplication.utils.ads.interfaces.AdShowCallback
import java.util.Date

/**
 * Base class cho tất cả loại quảng cáo.
 * @param T: kiểu object quảng cáo (AdView, InterstitialAd, v.v.)
 */
abstract class BaseAds<T> {

    var adObject: T? = null
    var isLoadingAd = false
    protected var loadTime: Date? = null

    companion object {
        // Test Ad Unit ID (AdMob official)
        const val TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"
        const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
        const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/3419835294"
        const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    }

    /** Load ad với callback trả kết quả */
    abstract fun loadAd(
        context: Context,
        adUnitId: String? = null,
        callback: AdLoadCallback? = null
    )

    /** Hiển thị ad (nếu có) */
    abstract fun showAd(
        activity: Activity,
        container: FrameLayout? = null,
        callback: AdShowCallback? = null
    )

    /** Kiểm tra ad đã được load chưa */
    fun isLoaded(): Boolean = adObject != null && !isAdExpired()

    /** Kiểm tra ad có đang được load không */
    fun isLoading(): Boolean = isLoadingAd

    /** Xóa ad khỏi bộ nhớ */
    open fun destroy() {
        adObject = null
        isLoadingAd = false
        loadTime = null
    }

    /** Kiểm tra ad có hết hạn không (4 giờ mặc định) */
    protected open fun isAdExpired(): Boolean {
        return loadTime?.let {
            val timeDifference = Date().time - it.time
            timeDifference > 4 * 60 * 60 * 1000 // 4 giờ
        } ?: false
    }

    /** Reset trạng thái loading */
    protected fun resetLoading() {
        isLoadingAd = false
    }
}