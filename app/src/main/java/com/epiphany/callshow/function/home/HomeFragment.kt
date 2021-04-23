package com.epiphany.callshow.function.home

import android.os.Bundle
import android.widget.FrameLayout
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    override fun getBindLayout(): Int = R.layout.fragment_home

    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun initView() {
        initStatusBarLayout()
    }

    private fun initStatusBarLayout() {
        context?.apply {
            StatusBarUtil.setTranslucentStatus(activity!!, true)
            val layoutParams = binding.rootView.layoutParams as FrameLayout.LayoutParams
            layoutParams.topMargin = SystemInfo.getStatusBarHeight(this)
            binding.rootView.layoutParams = layoutParams
        }
    }

    companion object {
        fun newInstance(bundle: Bundle? = null): HomeFragment {
            val fragment = HomeFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}