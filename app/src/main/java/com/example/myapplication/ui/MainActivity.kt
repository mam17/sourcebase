package com.example.myapplication.ui

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utils.NotificationUtil
import com.example.myapplication.utils.NotificationUtil.scheduleFullScreenNotificationAfterExit
import com.example.myapplication.utils.NotificationUtil.scheduleFullScreenNotificationDiary

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
        NotificationUtil.createNotificationChannel(this)

        viewBinding.btnPermission.setOnClickListener {
            ensureAndShowFullScreenNotification()
            Handler(mainLooper).postDelayed({
                scheduleFullScreenNotificationAfterExit(this)
                scheduleFullScreenNotificationDiary(this)
                finishAffinity()
            }, 2000)
        }
    }

    // 👉 Hàm bạn hỏi: xin quyền notification nếu cần, rồi gọi callback
    private fun requestNotificationPermissionsIfNeeded(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                onGranted()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                pendingAfterPermissionGranted = onGranted
            }
        } else {
            onGranted()
        }
    }

    // 👉 Hàm kiểm tra / xin quyền Full Screen Intent
    private fun ensureAndShowFullScreenNotification() {
        requestNotificationPermissionsIfNeeded {
            if (Build.VERSION.SDK_INT >= 34) {
                val nm = getSystemService(NotificationManager::class.java)
                if (nm != null && !nm.canUseFullScreenIntent()) {
                    openManageAppUseFullScreenIntentSettings()
                } else {
                    NotificationUtil.showFullScreenNotification(this)
                }
            } else {
                NotificationUtil.showFullScreenNotification(this)
            }
        }
    }

    private fun openManageAppUseFullScreenIntentSettings() {
        if (Build.VERSION.SDK_INT >= 34) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    // Launchers
    private var pendingAfterPermissionGranted: (() -> Unit)? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingAfterPermissionGranted?.invoke()
        } else {
            Toast.makeText(
                this,
                "Ứng dụng cần quyền thông báo để hiển thị cảnh báo toàn màn hình!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
