package com.example.myapplication.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.utils.NotificationUtil

class FullScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Khi tới giờ, hiển thị notification toàn màn hình
        NotificationUtil.showFullScreenNotification(context)
    }
}