package com.epiphany.callshow.function.main

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.ActivityMainBinding
import com.epiphany.callshow.function.home.HomeFragment
import com.epiphany.callshow.function.personal.PersonalFragment
import com.epiphany.callshow.function.video.VideoListFragment

class MainActivity : BaseActivity<BaseViewModel, ActivityMainBinding>() {
    //页面显示的所有Fragment
    private val mHomFragments = mutableMapOf<Int, Fragment>()

    //Tab的最后一个展示的序号
    private var mTabLastIndex = -1

    override fun getBindLayout(): Int = R.layout.activity_main

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        initLayout()
    }


    private fun initLayout() {
        binding.llBottomBar.itemIconTintList = null
        binding.llBottomBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> setFragmentPosition(TAB_HOME)
                R.id.my -> setFragmentPosition(TAB_PERSONAL)
                R.id.video -> setFragmentPosition(TAB_VIDEO)
            }
            true
        }
        //默认选中的TAB
        setFragmentPosition(TAB_HOME)
    }

    /**
     * 设置页面的布局展示
     */
    private fun setFragmentPosition(position: Int) {

        //判断如果相同时，则直接返回
        if (mTabLastIndex == position) {
            return
        }

        var fragment = mHomFragments[position]
        val lastFragment = mHomFragments[mTabLastIndex]
        val beginTransaction = supportFragmentManager.beginTransaction()
        //判断对象为空时，则创建对象
        if (fragment == null) {
            fragment = when (position) {
                TAB_HOME -> HomeFragment.newInstance()
                TAB_VIDEO -> VideoListFragment.newInstance()
                else -> PersonalFragment.newInstance()
            }
            mHomFragments[position] = fragment
        }
        //判断如果未添加时，则添加到FragmentManager中
        if (!fragment.isAdded) {
            beginTransaction.add(R.id.fl_content, fragment)
        }
        beginTransaction.show(fragment)
        beginTransaction.commitAllowingStateLoss()

        //判断如果上一次的不为空，则隐藏上一次的Fragment并调用onPause
        if (lastFragment != null) {
            beginTransaction.hide(lastFragment)
        }
        mTabLastIndex = position
        //调用状态
        val stateBeginTransaction = supportFragmentManager.beginTransaction()
        stateBeginTransaction.setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        if (lastFragment != null) {
            stateBeginTransaction.setMaxLifecycle(lastFragment, Lifecycle.State.STARTED)
        }
        stateBeginTransaction.commit()

    }

    override fun onDestroy() {
        super.onDestroy()
        mHomFragments.clear()
    }

    companion object {
        private const val TAB_HOME = 0
        private const val TAB_PERSONAL = 1
        private const val TAB_VIDEO = 2
    }

}