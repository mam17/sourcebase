package com.example.myapplication.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.utils.NotificationUtil

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationUtil.scheduleFullScreenNotificationDiary(context)
        }
    }
}
