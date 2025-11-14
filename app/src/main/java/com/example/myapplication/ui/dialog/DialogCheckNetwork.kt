package com.example.myapplication.ui.dialog

import android.content.Context
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogCheckInternetBinding

class DialogCheckNetwork(context: Context) : BaseDialog<DialogCheckInternetBinding>(context) {
    var actionTryAgain: (() -> Unit)? = null
    var actionExit: (() -> Unit)? = null
    override fun provideViewBinding(): DialogCheckInternetBinding {
        return DialogCheckInternetBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        setCancelable(false)

        viewBinding.btnCancel.setOnClickListener {
            actionExit?.invoke()
            dismiss()
        }
        viewBinding.btnTryAgain.setOnClickListener {
            actionTryAgain?.invoke()
            dismiss()
        }
    }
}