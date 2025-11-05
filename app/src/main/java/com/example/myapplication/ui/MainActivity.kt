package com.example.myapplication.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utils.NotificationUtil
import com.example.myapplication.utils.NotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun provideViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tạo notification channel trước
        NotificationUtil.createNotificationChannel(this)
    }

    override fun initViews() {
        super.initViews()
        // Gọi showNotification khi activity được tạo
        viewBinding.btnPermission.setOnClickListener {
            scheduleNotification()
        }
    }
    private fun scheduleNotification() {
        Log.d("MainActivity", "Lên lịch notification sau 30 giây...")

        // Tạo work request với delay 30 giây
        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(30, TimeUnit.SECONDS)
            .addTag("auto_notification")
            .build()

        // Lên lịch work
        WorkManager.getInstance(this).enqueue(notificationWork)

        Toast.makeText(this, "Thông báo sẽ hiển thị sau 30 giây khi thoát app", Toast.LENGTH_LONG).show()

        // Đóng app sau 2 giây để demo
        Handler(mainLooper).postDelayed({
            finishAffinity() // Đóng toàn bộ app
        }, 2000)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Có thể thêm logic ở đây nếu muốn tự động lên lịch khi activity bị destroy
        Log.d("MainActivity", "Activity đang bị destroy")
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationUtil.showFullScreenNotification(this)
        } else {
            Toast.makeText(this, "Quyền bị từ chối", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_FULL_SCREEN_INTENT
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationUtil.showFullScreenNotification(this)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.USE_FULL_SCREEN_INTENT)
                }
            }
        } else {
            NotificationUtil.showFullScreenNotification(this)
        }
    }

    private fun showNotification() {
        checkAndRequestPermission()
    }
}