package com.epiphany.callshow.function.home

import android.os.Bundle
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentHomeBinding
import com.google.android.material.appbar.AppBarLayout

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    private var mHomePageAdapter: HomePageAdapter? = null
    override fun getBindLayout(): Int = R.layout.fragment_home

    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun initView() {
        initLayout()
        initDataObserver()
    }

    private fun initLayout() {
        mHomePageAdapter = HomePageAdapter(childFragmentManager)
        binding.viewPager.adapter = mHomePageAdapter
        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //当切换Tab时，则展会默认的Tab展示
                binding.appbar.setExpanded(true, false)
            }

        })
        initStatusBarLayout()
    }

    private fun initDataObserver() {
        viewModel.getTabDataList().observe(this, {
            mHomePageAdapter?.setDataList(it)
            //设置绑定到Tab上
            binding.tabLayout.setViewPager(binding.viewPager)
            //设置缓冲的大小
            binding.viewPager.offscreenPageLimit = (it.size * .3f).toInt()
        })
        viewModel.loadTabData()
    }

    private fun initStatusBarLayout() {
        context?.apply {
            StatusBarUtil.setTranslucentStatus(activity!!, true)
            val layoutParams = binding.tabLayout.layoutParams as AppBarLayout.LayoutParams
            layoutParams.topMargin = SystemInfo.getStatusBarHeight(this)
            binding.tabLayout.layoutParams = layoutParams
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHomePageAdapter?.releaseData()
    }

    companion object {
        fun newInstance(bundle: Bundle? = null): HomeFragment {
            val fragment = HomeFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}