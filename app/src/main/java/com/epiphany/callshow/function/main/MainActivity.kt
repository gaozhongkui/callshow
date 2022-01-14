package com.epiphany.callshow.function.main

import androidx.viewpager.widget.ViewPager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.ActivityMainBinding

class MainActivity : BaseActivity<BaseViewModel, ActivityMainBinding>() {

    override fun getBindLayout(): Int = R.layout.activity_main

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        initLayout()
    }

    private fun initLayout() {
        binding.viewPager.adapter = MainFragmentAdapter(supportFragmentManager)
        binding.llBottomBar.setOnNavigationItemSelectedListener { item ->
            binding.viewPager.currentItem = when (item.itemId) {
                R.id.home -> 0
                R.id.video -> 1
                else -> 2
            }
            true
        }
        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                binding.llBottomBar.menu.getItem(position).isChecked = true
            }
        })
    }


}