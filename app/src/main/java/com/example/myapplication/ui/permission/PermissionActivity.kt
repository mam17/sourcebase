package com.example.myapplication.ui.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityPermissionBinding
import com.example.myapplication.ui.MainActivity

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {

    override fun provideViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(layoutInflater)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, PermissionActivity::class.java))
        }
    }

    override fun initViews() {
        super.initViews()


        viewBinding.apply {
            tvNext.setOnClickListener {
                MainActivity.start(this@PermissionActivity)
                finish()
            }
        }
    }
}