package com.example.myapplication.ui.language

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityLanguageBinding
import com.example.myapplication.libads.utils.NativeAdsUtil
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.ui.onboarding.OnBoardingActivity
import com.example.myapplication.utils.AppEx.observeOnce
import com.example.myapplication.utils.AppEx.setAppLanguage
import com.example.myapplication.utils.Constant.KEY_FROM_SPLASH
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.invisible
import com.example.myapplication.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels()
    private val languageAdapter = LanguageAdapter()
    private var fromSplash = false
    override fun provideViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(layoutInflater)
    }

    companion object {
        fun start(context: Context, fromSplash: Boolean = false) {
            Intent(context, LanguageActivity::class.java).also {
                it.putExtra(KEY_FROM_SPLASH, fromSplash)
                context.startActivity(it)
            }
        }
    }

    override fun initViews() {
        fromSplash = intent.getBooleanExtra(KEY_FROM_SPLASH, false)
        initNativeAd1()
        loadOnBoardingNativeAd()

        viewBinding.apply {
            if (!fromSplash) {
                toolBarLanguage.btnBack.visible()
            } else {
                toolBarLanguage.btnBack.gone()
            }

            toolBarLanguage.btnBack.setOnClickListener {
                finish()
            }

            rclLanguage.adapter = languageAdapter

            if (isCheckOpenApp) {
                toolBarLanguage.btnSelect.setText(R.string.txt_next)
            } else {
                toolBarLanguage.btnSelect.setText(R.string.txt_select)
            }

            toolBarLanguage.btnSelect.setOnClickListener {
                languageAdapter.selectedLanguage()?.let { languageModel ->
                    spManager.saveLanguage(languageModel)
                    setAppLanguage(languageModel.languageCode)
                    if (fromSplash) {
                        OnBoardingActivity.start(this@LanguageActivity)
                    } else {
                        MainActivity.start(this@LanguageActivity)
                    }
                    finish()
                }
            }
        }

    }

    override fun initData() {
        viewModel.loadListLanguage()
        languageAdapter.onClick = {
            viewBinding.prLoading.visible()
            viewBinding.toolBarLanguage.btnSelect.invisible()
            initNativeAd2()
            languageAdapter.selectLanguage(it.languageCode)

            lifecycleScope.launch {
                delay(3000)
                viewBinding.prLoading.gone()
                viewBinding.toolBarLanguage.btnSelect.visible()
            }
        }
    }

    override fun initObserver() {
        viewModel.listLanguage.observe(this) { listLang ->
            languageAdapter.setData(ArrayList(listLang))
            if (!fromSplash) {
                val currentLanguage = spManager.getLanguage()
                listLang.find { it.languageCode == currentLanguage.languageCode }?.selected = true
            }
        }
    }

    private fun initNativeAd1() {
        NativeAdsUtil.loadNativeLanguage1(isCheckOpenApp) { nativeAd ->
            nativeAd.getNativeAdLive().observeOnce(this@LanguageActivity) {
                if (nativeAd.available()) {
                    viewBinding.vNativeLanguage.frAdsNative.visible()
                    nativeAd.showNative(
                        viewBinding.vNativeLanguage.frAdsNative,
                        R.id.native_ad_view3,
                        null
                    )
                } else {
                    viewBinding.vNativeLanguage.frAdsNative.gone()
                }
            }
        }
    }

    private fun initNativeAd2() {
        NativeAdsUtil.loadNativeLanguage2(isCheckOpenApp) { nativeAd ->
            nativeAd.getNativeAdLive().observeOnce(this@LanguageActivity) {
                if (nativeAd.available()) {
                    viewBinding.vNativeLanguage.frAdsNative.gone()
                    viewBinding.vNativeLanguage.frAdsNative2.visible()
                    nativeAd.showNative(
                        viewBinding.vNativeLanguage.frAdsNative2,
                        R.id.native_ad_view1,
                        null
                    )
                } else {
                    viewBinding.vNativeLanguage.frAdsNative2.gone()
                }
            }
        }
    }

    private fun loadOnBoardingNativeAd() {
        if (fromSplash) {
            NativeAdsUtil.loadNativeFullScreenOnb(isCheckOpenApp)
            NativeAdsUtil.loadNativeFullScreenOnb2(isCheckOpenApp)
        }
    }
}