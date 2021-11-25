package com.epiphany.callshow.function.main

import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.ActivityMainBinding
import com.epiphany.callshow.function.home.HomeFragment

class MainActivity : BaseActivity<BaseViewModel, ActivityMainBinding>() {

    override fun getBindLayout(): Int = R.layout.activity_main

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        initLayout()
    }

    private fun initLayout() {
        showHomeFragment()
    }

    private fun showHomeFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_content, HomeFragment.newInstance())
        transaction.commit()
    }


}