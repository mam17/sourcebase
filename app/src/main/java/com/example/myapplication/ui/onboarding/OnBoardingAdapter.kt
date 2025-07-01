package com.example.myapplication.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemOnboardingBinding
import com.example.myapplication.domain.layer.OnboardingModel

class OnBoardingAdapter : BaseAdapter<OnboardingModel, ItemOnboardingBinding>() {

    override fun binData(viewBinding: ItemOnboardingBinding, item: OnboardingModel, position: Int) {
        viewBinding.apply {
            imgBoarding.setImageResource(item.resImage)
            tvOnboarding.text = root.context.resources.getString(item.resDescription)
        }
    }

    override fun provideViewBinding(parent: ViewGroup): ItemOnboardingBinding =
        ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
}