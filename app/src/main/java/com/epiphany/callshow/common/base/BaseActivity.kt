package com.epiphany.callshow.common.base

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity<T : ViewModel, S : ViewDataBinding> : AppCompatActivity() {
    protected lateinit var binding: S
    protected lateinit var viewModel: T

    abstract fun getBindLayout(): Int

    abstract fun getViewModelClass(): Class<T>

    abstract fun initView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindow(this)
        binding = DataBindingUtil.setContentView(this, getBindLayout())
        viewModel = ViewModelProvider(this).get(getViewModelClass())
        initView()
    }

    private fun setupWindow(activity: Activity?) {
        if (activity != null && activity.window != null) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.window.statusBarColor = Color.TRANSPARENT
            }
        }
    }

}