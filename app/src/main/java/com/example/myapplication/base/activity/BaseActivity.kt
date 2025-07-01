package com.example.myapplication.base.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R
import com.example.myapplication.domain.layer.LanguageModel
import com.example.myapplication.ui.dialog.DialogLoading
import com.example.myapplication.utils.LocaleHelper
import com.example.myapplication.utils.SpManager
import java.util.Locale


abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {
    lateinit var viewBinding: V
    lateinit var language: LanguageModel
    private var dialogLoading: DialogLoading? = null
    lateinit var spManager: SpManager
    var isCheckOpenApp = false

    open fun onBack() {
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        spManager = SpManager.getInstance(newBase)
        language = spManager.getLanguage()
        val context = LocaleHelper.setLocale(newBase, language.languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCheckOpenApp = spManager.getFirstOpenApp()
        Log.i("TAG_CHECK_OPEN_APP", "onCreate: $isCheckOpenApp")
        //language setting
        setupLanguage()

        //viewBinding setting
        viewBinding = provideViewBinding()
        setContentView(viewBinding.root)

        //status bar setting
        hideSystemUI()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true

        // Call initialization methods
        initViews()
        initData()
        initObserver()

        //Handle Back Button Action
        handleOnBackPressed()

    }

    private fun setupLanguage() {
        val language: String = SpManager.getInstance(this).getLanguage().languageCode
        if (language.isNotEmpty()) {
            val locale = Locale(language.lowercase(Locale.getDefault()))
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            createConfigurationContext(config)
        }
    }

    open fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    fun replaceFragment(id: Int, fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            addToBackStack(fragment::class.java.simpleName)
            replace(id, fragment, fragment::class.java.simpleName)
        }
    }

    fun removeFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            remove(fragment)
        }
    }

    fun addFragment(id: Int, fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            addToBackStack(fragment::class.java.simpleName)
            add(id, fragment, fragment::class.java.simpleName)
        }
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            show(fragment)
        }
    }

    fun hideFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            hide(fragment)
        }
    }

    abstract fun provideViewBinding(): V

    open fun initViews() {}
    open fun initData() {}
    open fun initObserver() {}

    fun showLoading(strText: String = getString(R.string.txt_loading)) {
        if (dialogLoading == null) {
            dialogLoading = DialogLoading(this, strText)
        }
        dialogLoading?.show()
    }

    fun hideLoading() {
        dialogLoading?.dismiss()
        dialogLoading = null
    }

    fun showToast(mes: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, mes, duration).show()
    }

    fun setStatusBarColor(colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        window.statusBarColor = color
        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            controller.isAppearanceLightStatusBars = true
        }
    }

    fun setFullscreen() {
        applyFitsSystemWindows(viewBinding.root.rootView, false)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true
    }

    private fun hideSystemUI() {
        applyFitsSystemWindows(viewBinding.root.rootView, true)
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }

    private fun applyFitsSystemWindows(view: View?, fitSystemWindows: Boolean) {
        if (view is ViewGroup) {
            ViewCompat.setFitsSystemWindows(view, fitSystemWindows)
            for (i in 0 until view.childCount) {
                applyFitsSystemWindows(view.getChildAt(i), fitSystemWindows)
            }
        }
    }

}