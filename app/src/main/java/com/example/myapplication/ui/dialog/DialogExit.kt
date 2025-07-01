package com.example.myapplication.ui.dialog

import android.content.Context
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogExitBinding

class DialogExit(context: Context) :
    BaseDialog<DialogExitBinding>(context) {
    var yesOnClick: (() -> Unit)? = null
    override fun provideViewBinding(): DialogExitBinding {
        return DialogExitBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        viewBinding.apply {
            btnYes.setOnClickListener {
                yesOnClick?.invoke()
                dismiss()
            }
            btnCancel.setOnClickListener { dismiss() }
        }
    }
}