package com.example.myapplication.ui.onboarding

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityOnBoardingBinding
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

        viewModel.loadListOnBoarding()
        viewBinding.apply {
            vpOnBoarding.adapter = mAdapter
            vpOnBoarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition = position

                    if (currentPosition == 2) {
                        viewBinding.buttonNext.setText(R.string.txt_get_start)
                        switchBtnNext(true)
                    } else {
                        viewBinding.buttonNext.setText(R.string.txt_next)
                        switchBtnNext(false)
                    }
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

            btnStart.setOnClickListener {
                PermissionActivity.start(this@OnBoardingActivity)
                finish()
            }
        }
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.listOnBoarding.observe(this) {
            mAdapter.setData(it)
        }
    }

    private fun switchBtnNext(boolean: Boolean) {
        if (boolean) {
            viewBinding.rlDotNext.gone()
            viewBinding.btnStart.visible()
        } else {
            viewBinding.rlDotNext.visible()
            viewBinding.btnStart.gone()
        }
    }
}