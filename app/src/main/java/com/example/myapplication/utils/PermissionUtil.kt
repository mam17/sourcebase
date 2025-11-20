package com.example.myapplication.utils

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

object PermissionUtil {
//    // 👉 Hàm bạn hỏi: xin quyền notification nếu cần, rồi gọi callback
//    private fun requestNotificationPermissionsIfNeeded(context: Context, onGranted: () -> Unit) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(
//                    context, Manifest.permission.POST_NOTIFICATIONS
//                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
//            ) {
//                onGranted()
//            } else {
//                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                pendingAfterPermissionGranted = onGranted
//            }
//        } else {
//            onGranted()
//        }
//    }
//
//    // 👉 Hàm kiểm tra / xin quyền Full Screen Intent
//    private fun ensureAndShowFullScreenNotification(context: Context) {
//        requestNotificationPermissionsIfNeeded(context) {
//            if (Build.VERSION.SDK_INT >= 34) {
//                val nm = context.getSystemService(NotificationManager::class.java)
//                if (nm != null && !nm.canUseFullScreenIntent()) {
//                    openManageAppUseFullScreenIntentSettings()
//                } else {
//                    NotificationUtil.showFullScreenNotification(context)
//                }
//            } else {
//                NotificationUtil.showFullScreenNotification(context)
//            }
//        }
//    }
//
//    private fun openManageAppUseFullScreenIntentSettings(context: Context, ) {
//        if (Build.VERSION.SDK_INT >= 34) {
//            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
//                data = Uri.parse("package:$packageName")
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            context.startActivity(intent)
//        }
//    }
//
//    // Launchers
//    private var pendingAfterPermissionGranted: (() -> Unit)? = null
//
//    private val notificationPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { granted ->
//        if (granted) {
//            pendingAfterPermissionGranted?.invoke()
//        } else {
//            Toast.makeText(
//                context,
//                "Ứng dụng cần quyền thông báo để hiển thị cảnh báo toàn màn hình!",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }


}