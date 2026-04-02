package com.example.myapplication.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.myapplication.R
import com.example.myapplication.base.adapter.simple.BaseAdapter
import com.example.myapplication.databinding.ItemOnboardingBinding
import com.example.myapplication.domain.layer.OnboardingModel
import com.example.myapplication.domain.layer.OnboardingModel.Companion.FULL_NATIVE_FLAG
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible

class OnBoardingAdapter : BaseAdapter<OnboardingModel, ItemOnboardingBinding>() {

    override fun binData(viewBinding: ItemOnboardingBinding, item: OnboardingModel, position: Int) {
        viewBinding.apply {
            viewBinding.apply {
                layoutNativeFSTimer.tvTimeCount.gone()
                if (item.resImage == FULL_NATIVE_FLAG) {
                    ctContOnb.gone()
                    layoutNativeFSTimer.shimmerNativeFullScreen.visible()
                    item.nativeFull?.let {
                        it.showNative(
                            layoutNativeFSTimer.shimmerNativeFullScreen,
                            R.id.native_ad_view,
                            null
                        )
                        layoutNativeFSTimer.btnCloseOnb.postDelayed({
                            layoutNativeFSTimer.rlCloseAds.visible()
                            layoutNativeFSTimer.btnCloseOnb.visible()
                        }, 2000)
                    }
                } else {
                    layoutNativeFSTimer.shimmerNativeFullScreen.gone()
                    layoutNativeFSTimer.rlCloseAds.gone()
                    ctContOnb.visible()

                    imgBoarding.setImageResource(item.resImage)
                    tvTitle.text = root.context.resources.getString(item.resTitle)
                    tvOnboarding.text = root.context.resources.getString(item.resDescription)
                }

                layoutNativeFSTimer.btnCloseOnb.setOnClickListener {
                    onClick?.invoke(item)
                }
            }
        }
    }

    override fun provideViewBinding(parent: ViewGroup): ItemOnboardingBinding =
        ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
}