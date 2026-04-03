package com.example.myapplication.ui.onboarding

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityOnBoardingBinding
import com.example.myapplication.domain.layer.OnboardingModel
import com.example.myapplication.domain.layer.OnboardingModel.Companion.FULL_NATIVE_FLAG
import com.example.myapplication.libads.utils.NativeAdsUtil
import com.example.myapplication.ui.permission.PermissionActivity
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, OnBoardingActivity::class.java))
        }
    }

    override fun provideViewBinding(): ActivityOnBoardingBinding =
        ActivityOnBoardingBinding.inflate(layoutInflater)

    private val viewModel: OnBoardingViewModel by viewModels()
    private var mAdapter = OnBoardingAdapter()
    private var currentPosition = 0

    override fun initViews() {
        super.initViews()
        setFullScreen()
        viewModel.loadListOnBoarding()
        viewBinding.apply {
            vpOnBoarding.adapter = mAdapter
            vpOnBoarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition = position
                    initAdsFullOnBoarding()
                }
            })
            dotIndicator.attachTo(vpOnBoarding)

            buttonNext.setOnClickListener {
                if (currentPosition < mAdapter.getListData().size - 1) {
                    vpOnBoarding.setCurrentItem(currentPosition + 1, true)
                } else {
                    PermissionActivity.start(this@OnBoardingActivity)
                    finish()
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mAdapter.onClick = { item ->
            if (currentPosition < mAdapter.getListData().size - 1) {
                viewBinding.vpOnBoarding.setCurrentItem(currentPosition + 1, true)
            } else {
                PermissionActivity.start(this@OnBoardingActivity)
                finish()
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.listOnBoarding.observe(this) {
            initAdsFullOnBoarding(it)
        }
    }

    private fun initAdsFullOnBoarding(boardings: List<OnboardingModel>) {
        val list = ArrayList(boardings)

        val nativeAdHelper = NativeAdsUtil.onbNativeFullScreenAdmob
        if (nativeAdHelper != null) {
            val adsOnboard = OnboardingModel(
                FULL_NATIVE_FLAG,
                FULL_NATIVE_FLAG,
                FULL_NATIVE_FLAG,
                nativeAdHelper
            )
            val insertIndex = minOf(1, list.size)
            list.add(insertIndex, adsOnboard)
        } else {
            Log.w("TAG_ONBDING", "Không có nativeFullScreenOnb, bỏ qua chèn quảng cáo.")
        }

        val nativeAdHelper2 = NativeAdsUtil.onbNativeFullScreenAdmob2
        if (nativeAdHelper2 != null) {
            val adsOnboard = OnboardingModel(
                FULL_NATIVE_FLAG,
                FULL_NATIVE_FLAG,
                FULL_NATIVE_FLAG,
                nativeAdHelper2
            )

            val insertIndex = if (list.size == 4) minOf(3, list.size) else minOf(2, list.size)

            list.add(insertIndex, adsOnboard)
        } else {
            Log.w("TAG_ONBDING", "Không có nativeAdHelper2, bỏ qua chèn quảng cáo.")
        }

        mAdapter.setData(list)
    }

    private fun initAdsFullOnBoarding() = viewBinding.apply {
        if (mAdapter.dataSet.isEmpty()) return@apply
        dotIndicator.visible()
        buttonNext.visible()
        navigationLayout.visible()
        viewBinding.buttonNext.setText(R.string.txt_next)
        when (mAdapter.dataSet[currentPosition].resImage) {
            R.drawable.ic_lang_english -> {
                Log.i("TAG_ONBDING", "initMultiNativeAd: onb 1")
            }

            R.drawable.ic_lang_vietnamese -> {
                Log.i("TAG_ONBDING", "initMultiNativeAd: onb 2")
            }

            R.drawable.ic_lang_hindi -> {
                Log.i("TAG_ONBDING", "initMultiNativeAd: onb 3")
                viewBinding.buttonNext.setText(R.string.txt_get_start)
            }

            FULL_NATIVE_FLAG -> {
                Log.i("TAG_ONBDING", "initMultiNativeAd: native full")
                dotIndicator.gone()
                buttonNext.gone()
                navigationLayout.gone()
            }
        }
    }
}