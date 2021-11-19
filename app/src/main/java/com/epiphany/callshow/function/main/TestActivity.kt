package com.epiphany.callshow.function.main

import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.ActivityTestLayoutBinding

class TestActivity : BaseActivity<BaseViewModel, ActivityTestLayoutBinding>() {
    override fun getBindLayout(): Int {
        return R.layout.activity_test_layout
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("main")

        val test = module.callAttr(
            "search"
        )
        Log.d(TAG, "initView() called" + test)

    }


    companion object {
        private const val TAG = "TestActivity"
    }

}

