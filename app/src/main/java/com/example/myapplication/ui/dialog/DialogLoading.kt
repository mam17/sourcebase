package com.example.myapplication.ui.dialog

import android.content.Context
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogLoadingBinding

class DialogLoading(context: Context, private val strTitle: String) :
    BaseDialog<DialogLoadingBinding>(context) {
    override fun provideViewBinding(): DialogLoadingBinding {
        return DialogLoadingBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        viewBinding.apply {
            tvTitleLoading.text = strTitle
        }
    }
}