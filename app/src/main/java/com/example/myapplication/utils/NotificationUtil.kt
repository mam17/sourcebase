package com.example.myapplication.utils

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.ui.ui.FullScreenActivity

object NotificationUtil {
     fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "FULL_SCREEN_CHANNEL",
                "Full Screen Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for full screen notifications"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

     fun createFullScreenIntent(activity: Context): PendingIntent {
        val fullScreenIntent = Intent(activity, FullScreenActivity::class.java)
        return PendingIntent.getActivity(
            activity,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Trong NotificationUtil
    fun showFullScreenNotification(context: Context) {
        println("DEBUG: Bắt đầu hiển thị notification")

        val notificationBuilder = NotificationCompat.Builder(context, "FULL_SCREEN_CHANNEL")
            .setSmallIcon(R.drawable.ic_lang_vietnamese)
            .setContentTitle("Thông báo quan trọng")
            .setContentText("Đây là thông báo full màn hình")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(createFullScreenIntent(context), true)
            .setAutoCancel(true)
            .setContentIntent(createContentIntent(context))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())

        println("DEBUG: Đã hiển thị notification")
    }

     fun createContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}