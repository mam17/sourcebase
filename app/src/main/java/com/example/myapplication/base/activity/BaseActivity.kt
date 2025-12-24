package com.example.myapplication.base.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R
import com.example.myapplication.domain.layer.LanguageModel
import com.example.myapplication.libads.utils.FirebaseConfigManager
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
    val adsConfig = FirebaseConfigManager.instance().adConfig

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
        setupLanguage()

        viewBinding = provideViewBinding()
        setContentView(viewBinding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        hideNavigationBar()

        initViews()
        initData()
        initObserver()

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
            setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
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

    fun setStatusBarColor(lightIcons: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = lightIcons
    }

    fun hideNavigationBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun setFullScreen() {
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

    }
}