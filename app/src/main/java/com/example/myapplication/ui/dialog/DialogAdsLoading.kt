package com.example.myapplication.ui.dialog

import android.content.Context
import android.graphics.Color
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toDrawable
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogLoadingFullBinding

class DialogAdsLoading(context: Context) :
    BaseDialog<DialogLoadingFullBinding>(context) {
    override fun provideViewBinding(): DialogLoadingFullBinding {
        return DialogLoadingFullBinding.inflate(layoutInflater)
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