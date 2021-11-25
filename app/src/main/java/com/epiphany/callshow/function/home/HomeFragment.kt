package com.epiphany.callshow.function.home

import android.os.Bundle
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentHomeBinding
import com.epiphany.callshow.function.search.SearchVideoActivity

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
        initStatusBarLayout()

        binding.ivSearch.setOnClickListener {
            SearchVideoActivity.launch(requireContext())
        }
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
            SystemInfo.fixStatusBar(binding.toolBar, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHomePageAdapter = null
    }

    companion object {
        fun newInstance(bundle: Bundle? = null): HomeFragment {
            val fragment = HomeFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}