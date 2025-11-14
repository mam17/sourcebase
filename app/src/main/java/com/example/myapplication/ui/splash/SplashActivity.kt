package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.utils.ads.AdsManager
import com.example.myapplication.utils.ads.admods.BannerAds
import com.example.myapplication.utils.ads.interfaces.AdLoadCallback
import com.example.myapplication.utils.ads.interfaces.AdShowCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val bannerAds = BannerAds()
    private val TAG = "TAG_SplashActivity"

    override fun provideViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()


        // Khởi tạo consent manager
        App.instance?.initConsentManager(this)

        // Kiểm tra consent và load banner
        checkConsentAndLoadAds()
    }

    private fun checkConsentAndLoadAds() {
        val consentManager = com.example.myapplication.libads.consent.GoogleMobileAdsConsentManager.getInstance(this)

        if (consentManager.canRequestAds()) {
            initializeMobileAdsSdk()
            Log.i(TAG, "checkConsentAndLoadAds: true")
        } else {
            Log.i(TAG, "checkConsentAndLoadAds: false")
//            proceedToMainScreen()
            initializeMobileAdsSdk()

        }

    }

    private fun initializeMobileAdsSdk() {
        // Gọi load quảng cáo
        initBannerAds()
        proceedToMainScreen()


    }


    private fun initBannerAds() {
        AdsManager.loadBanner(this, null, object : AdLoadCallback {
            override fun onAdLoaded() {
                // show banner khi load xong
                AdsManager.showBanner(this@SplashActivity, viewBinding.adViewContainer, object : AdShowCallback {
                    override fun onAdShown() {
                        Log.d("Banner", "Banner hiển thị thành công")
                    }

                    override fun onAdFailedToShow() {
                        Log.e("Banner", "Không thể hiển thị banner")
                    }
                })
            }

            override fun onAdFailed(error: com.google.android.gms.ads.LoadAdError) {
                Log.e("Banner", "Load banner thất bại: ${error.message}")
            }
        })
    }


    private fun proceedToMainScreen() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            gotoMainScreen()
        }
    }

    private fun gotoMainScreen() {
        MainActivity.start(this)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerAds.destroy()
    }
}