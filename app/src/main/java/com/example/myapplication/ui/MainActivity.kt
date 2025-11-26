package com.example.myapplication.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.App
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.libads.interfaces.OnAdmobLoadListener
import com.example.myapplication.libads.interfaces.OnAdmobShowListener
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.AppEx.observeOnce
import com.example.myapplication.utils.NotificationUtil
import com.example.myapplication.utils.ads.AdPlacement
import com.example.myapplication.utils.ads.InterstitialAdsUtil
import com.example.myapplication.utils.ads.NativeAdsUtil
import com.example.myapplication.utils.ads.RewardedAdsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            idAds = BuildConfig.reward_create,
            adPlacement = "reward_feature",
            isEnable = true
        )
    }
    private val interSplash by lazy {
        InterstitialAdsUtil(
            context = this,
            idAds = BuildConfig.inter_splash,
            idAds2f = BuildConfig.inter_splash,
            adPlacement = AdPlacement.INTER_SPLASH,
            isEnable = true
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationUtil.createNotificationChannel(this)

        App.instance?.loadAdsOpenResume()

        NativeAdsUtil.loadNativeHome()
        showNativeHome()
        loadRewardMain()
        loadInterMain()

        viewBinding.btnInterSplash.setOnClickListener {
            showInterAdsSplash {
                LanguageActivity.start(this@MainActivity)
            }
        }


        viewBinding.btnReward.setOnClickListener {
            showRewardAds {
                LanguageActivity.start(this@MainActivity)
            }
        }
    }

    fun loadRewardMain() {
        rewardedAd.load(object : OnAdmobLoadListener {
            override fun onLoad() {
                Log.d("TAG_REWARD", "Rewarded loaded")
            }

            override fun onError(e: String) {
                Log.d("TAG_REWARD", "Rewarded load error: $e")
            }
        })
    }

    fun loadInterMain() {
        interSplash.load()
    }

    fun showRewardAds(action: () -> Unit) {
        if (rewardedAd.isLoaded()) {
            rewardedAd.show(this, object : OnAdmobShowListener {
                override fun onShow() {
                    Log.d("TAG_REWARD", "Rewarded showed")
                    action.invoke()
                }

                override fun onError(e: String) {
                    Log.d("TAG_REWARD", "Show rewarded error: $e")
                    showToast("Load ads failed")
                }
            })
        } else {
            Log.d("TAG_REWARD", "Rewarded ad not loaded yet")
            showToast("Load ads failed")
        }
    }

    fun showInterAdsSplash(action: () -> Unit) {
        interSplash.show(this, object : OnAdmobShowListener {
            override fun onShow() {
                Log.d("tag_ADS", "splash inter showed")
                action.invoke()
            }

            override fun onError(e: String) {
                Log.d("tag_ADS", "Show error splash: $e")
                action.invoke()
            }
        })
    }

    private fun showNativeHome() {
        lifecycleScope.launch(Dispatchers.Main) {
            NativeAdsUtil.homeNativeAdmob?.run {
                getNativeAdLive().observeOnce(this@MainActivity) {
                    if (available()) {
                        showNative(viewBinding.flAdplaceholder, R.id.native_ad_view, null)
                    }
                }
            }
        }
    }
}
