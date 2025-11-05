package com.example.myapplication.utils

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.utils.NotificationUtil

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("NotificationWorker", "Bắt đầu hiển thị notification sau 30 giây")
        
        try {
            NotificationUtil.showFullScreenNotification(applicationContext)
            Log.d("NotificationWorker", "Đã hiển thị notification thành công")
            return Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Lỗi khi hiển thị notification: ${e.message}")
            return Result.failure()
        }
    }
}