package com.example.myapplication.libads.helper

import android.content.Context
import android.graphics.Color
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toDrawable
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogAdsLoadingBinding

class DialogAdsLoading(context: Context) :
    BaseDialog<DialogAdsLoadingBinding>(context) {
    override fun provideViewBinding(): DialogAdsLoadingBinding {
        return DialogAdsLoadingBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        window?.setBackgroundDrawable(Color.WHITE.toDrawable())
        window?.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        setCancelable(false)
    }
}