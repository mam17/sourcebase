package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.ui.language.LanguageActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun provideViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            gotoMainScreen()
        }
    }

    private fun gotoMainScreen() {
        LanguageActivity.start(this, true)
        finish()
    }
}