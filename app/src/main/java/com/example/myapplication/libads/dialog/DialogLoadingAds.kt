package com.example.myapplication.libads.dialog

import android.content.Context
import android.widget.RelativeLayout
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.LayoutAdsLoadingBinding

class DialogLoadingAds(context: Context) :
    BaseDialog<LayoutAdsLoadingBinding>(context) {
    override fun provideViewBinding(): LayoutAdsLoadingBinding {
        return LayoutAdsLoadingBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        setCancelable(false)
        window?.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
    }
}