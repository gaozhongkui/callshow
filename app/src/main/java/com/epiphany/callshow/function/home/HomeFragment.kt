package com.epiphany.callshow.function.home

import android.os.Bundle
import android.widget.FrameLayout
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentHomeBinding

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
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        initStatusBarLayout()
    }

    private fun initDataObserver() {
        viewModel.getTabDataList().observe(this, {
            mHomePageAdapter?.setDataList(it)
        })
        viewModel.loadTabData()
    }

    private fun initStatusBarLayout() {
        context?.apply {
            StatusBarUtil.setTranslucentStatus(activity!!, true)
            val layoutParams = binding.rootView.layoutParams as FrameLayout.LayoutParams
            layoutParams.topMargin = SystemInfo.getStatusBarHeight(this)
            binding.rootView.layoutParams = layoutParams
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