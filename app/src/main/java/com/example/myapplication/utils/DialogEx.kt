package com.example.myapplication.utils

import android.content.Context
import com.example.myapplication.ui.dialog.DialogAdsLoading
import com.example.myapplication.ui.dialog.DialogCheckNetwork

object DialogEx {
    fun Context.dialogCheckNetWork(
        actionExit: (() -> Unit)? = null,
        actionTryAgain: (() -> Unit)? = null
    ): DialogCheckNetwork {
        val dialogCheckNetWork = DialogCheckNetwork(this)
        dialogCheckNetWork.apply {
            show()
            this.actionExit = {
                actionExit?.invoke()
            }
            this.actionTryAgain = {
                actionTryAgain?.invoke()
            }
        }

        return dialogCheckNetWork
    }


}