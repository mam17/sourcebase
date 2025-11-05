package com.example.myapplication.ui.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class FullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hiển thị cửa sổ full screen
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        setContentView(R.layout.activity_full_screen)

        // Xử lý đóng activity khi người dùng tương tác
        findViewById<Button>(R.id.dismissButton).setOnClickListener {
            finish()
        }

        // Tự động đóng sau 10 giây
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 10000)
    }


}