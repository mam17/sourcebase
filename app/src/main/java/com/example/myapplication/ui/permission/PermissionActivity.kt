package com.example.myapplication.ui.permission

import android.content.Context
import android.content.Intent
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityPermissionBinding
import com.example.myapplication.libads.utils.NativeAdsUtil
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.utils.AppEx.observeOnce
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {

    override fun provideViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PermissionActivity::class.java))
        }
    }

    override fun initViews() {
        super.initViews()
        initNativeAd()

        viewBinding.apply {
            tvNext.setOnClickListener {
                MainActivity.start(this@PermissionActivity)
                finish()
            }
        }
    }

    private fun initNativeAd() {
        NativeAdsUtil.loadNativeFeature() { nativeAd ->
            nativeAd.getNativeAdLive().observeOnce(this@PermissionActivity) {
                if (nativeAd.available()) {
                    viewBinding.shimmerNativeFeature.visible()
                    nativeAd.showNative(viewBinding.shimmerNativeFeature, R.id.native_ad_view_feature, null)
                } else {
                    viewBinding.shimmerNativeFeature.gone()
                }
            }
        }
    }
}