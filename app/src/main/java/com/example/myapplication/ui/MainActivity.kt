package com.example.myapplication.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.libads.interfaces.OnAdmobShowListener
import com.example.myapplication.libads.utils.AdPlacement
import com.example.myapplication.libads.utils.AdsEx
import com.example.myapplication.libads.utils.InterstitialAdsUtil
import com.example.myapplication.libads.utils.NativeAdsUtil
import com.example.myapplication.libads.utils.RewardedAdsUtil
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.ui.ui.RewardActivity
import com.example.myapplication.utils.AppEx.observeOnce
import com.example.myapplication.utils.NotificationUtil
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun provideViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    private val rewardedAd by lazy {
        RewardedAdsUtil(
            context = this,
            idAds = AdsEx.getRewardId(BuildConfig.reward_create),
            adPlacement = "reward_feature",
            isEnable = true
        )
    }
    private val interHome by lazy {
        InterstitialAdsUtil(
            context = this,
            idAds = AdsEx.getInterstitialId(remoteConfig.inter_home.id),
            adPlacement = AdPlacement.INTER_HOME,
            isEnable = remoteConfig.inter_home.enabled
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtil.createNotificationChannel(this)
        spManager.saveFirstOpenApp()
        App.instance?.loadAdsOpenResume()

        viewBinding.btnBuyPro.setOnClickListener {
            startActivity(Intent(this, RewardActivity::class.java))
        }

        viewBinding.btnInterHome.setOnClickListener {
            showInterHome {
                LanguageActivity.start(this@MainActivity)
            }
        }

        viewBinding.btnInterHomePreload.setOnClickListener {
            showInterHome {
                LanguageActivity.start(this@MainActivity)
            }
        }

        viewBinding.btnReward.setOnClickListener {
            showRewardAds {
                LanguageActivity.start(this@MainActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPro) {
            viewBinding.btnBuyPro.gone()
            viewBinding.flAdplaceholder.gone()
        } else {
            viewBinding.btnBuyPro.visible()
            showNativeHome()
        }
    }

    fun showRewardAds(action: () -> Unit) {
        rewardedAd.show(this, object : OnAdmobShowListener {
            override fun onShow() {
                Log.d("TAG_REWARD", "Rewarded showed")
                action.invoke()
            }

            override fun onError(e: String) {
                Log.d("TAG_REWARD", "Show rewarded error: $e")
                showToast("The advertisement is not yet ready.")
                action.invoke() // Still let user proceed if Pro or for UX
            }
        }, true)
    }

    private fun showInterHome(action: () -> Unit) {
        interHome.show(this, object : OnAdmobShowListener {
            override fun onShow() {
                Log.d("TAG_INTER_HOME", "Home inter showed")
            }

            override fun onError(e: String) {
                Log.d("TAG_INTER_HOME", "Show error: $e")
                action.invoke()
            }

            override fun onClosed() {
                super.onClosed()
                Log.d("TAG_INTER_HOME", "Home inter closed")
                action.invoke()
            }
        }, true)
    }

    private fun showNativeHome() {
        NativeAdsUtil.loadNativeHome { nativeAd ->
            nativeAd.getNativeAdLive().observeOnce(this@MainActivity) {
                if (nativeAd.available()) {
                    viewBinding.flAdplaceholder.visible()
                    nativeAd.showNative(viewBinding.flAdplaceholder, R.id.native_ad_view, null)
                } else {
                    viewBinding.flAdplaceholder.gone()
                }
            }
        }
    }
}
