package com.example.myapplication.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.myapplication.base.activity.BaseActivity
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.let

abstract class BaseBottomFragment<V : ViewBinding> : BottomSheetDialogFragment() {

    private var _binding: V? = null
    protected val viewBinding get() = _binding!!
    var onClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = provideViewBinding(inflater, container)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.root.isClickable = true
        initViews()
        initData()
        initObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ================== BottomSheet control ==================

    /**
     * Set chiều cao bottom sheet theo tỉ lệ màn hình (0f..1f).
     * @param ratio 1f = full, 0.5f = nửa màn hình, ...
     */
    fun setBottomSheetHeight(ratio: Float = 1f) {
        dialog?.setOnShowListener { d ->
            val bottomSheet =
                (d as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                val layoutParams = it.layoutParams
                val screenHeight = resources.displayMetrics.heightPixels
                val targetHeight = (screenHeight * ratio).toInt()

                layoutParams.height = targetHeight
                it.layoutParams = layoutParams

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = targetHeight
            }
        }
    }

    /** ========== Forward các hàm hỗ trợ từ BaseActivity ========== */

    fun addFragment(id: Int, fragment: Fragment) {
        (activity as? BaseActivity<*>)?.addFragment(id, fragment)
    }

    fun replaceFragment(id: Int, fragment: Fragment) {
        (activity as? BaseActivity<*>)?.replaceFragment(id, fragment)
    }

    fun hideFragment(fragment: Fragment) {
        (activity as? BaseActivity<*>)?.hideFragment(fragment)
    }

    fun removeFragment(fragment: Fragment) {
        (activity as? BaseActivity<*>)?.removeFragment(fragment)
    }

    fun showLoading(message: String? = null) {
        if (message != null) {
            (activity as? BaseActivity<*>)?.showLoading(message)
        } else {
            (activity as? BaseActivity<*>)?.showLoading()
        }
    }

    fun hideLoading() {
        (activity as? BaseActivity<*>)?.hideLoading()
    }

    fun showToast(mes: String, duration: Int = Toast.LENGTH_SHORT) {
        (activity as? BaseActivity<*>)?.showToast(mes, duration)
    }

    /** ========== Abstract / override ========== */

    abstract fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?): V

    open fun initViews() {}
    open fun initData() {}
    open fun initObserver() {}
}
