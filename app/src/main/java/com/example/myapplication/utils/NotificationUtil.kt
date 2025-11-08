package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.R
import com.example.myapplication.broadcasts.FullScreenReceiver
import com.example.myapplication.ui.MainActivity
import com.example.myapplication.ui.ui.FullScreenActivity
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationUtil {

    private const val CHANNEL_ID = "FULL_SCREEN_CHANNEL"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Full Screen Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for full screen notifications"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }


    fun createFullScreenIntent(context: Context): PendingIntent {
        val intent = Intent(context, FullScreenActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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

    fun showFullScreenNotification(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val canFSI = if (Build.VERSION.SDK_INT >= 34) nm?.canUseFullScreenIntent() == true else true

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lang_vietnamese)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Đây là thông báo hiển thị full màn hình.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSound(Settings.System.DEFAULT_RINGTONE_URI)
            .setContentIntent(createContentIntent(context))

        if (canFSI) {
            builder.setFullScreenIntent(createFullScreenIntent(context), true)
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val settingsPending = PendingIntent.getActivity(
                context, 123, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Bật quyền toàn màn hình", settingsPending)
        }

        nm?.notify(1, builder.build())
    }


    fun scheduleFullScreenNotificationAfterExit(context: Context) {
        val work = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS) // sau 10 giây
            .addTag("full_screen_notification")
            .build()

        WorkManager.getInstance(context).enqueue(work)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleFullScreenNotificationDiary(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!ensureExactAlarmPermission(context)) {
            Toast.makeText(context, "Chưa có quyền alarm", Toast.LENGTH_LONG).show()
            return // ⬅️ Chưa có quyền thì không đặt báo thức
        }
        // Lên lịch 2 thời điểm mỗi ngày
        val times = listOf(
            getNextTriggerTime(9, 0),  // 9:00 sáng
            getNextTriggerTime(22, 55)  // 9:00 tối
        )

        for (triggerTime in times) {
            val intent = Intent(context, FullScreenReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                triggerTime.timeInMillis.toInt(), // ID khác nhau
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Dùng setExactAndAllowWhileIdle để chính xác hơn và chạy cả khi sleep
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Tính thời điểm kích hoạt tiếp theo cho giờ và phút cho trước.
     */
    private fun getNextTriggerTime(hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    private fun ensureExactAlarmPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val hasPermission = alarmManager.canScheduleExactAlarms()
            if (!hasPermission) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            return hasPermission
        }
        return true
    }

}
