package com.epiphany.callshow.function.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentHomeBinding
import com.epiphany.callshow.function.search.SearchVideoActivity
import kotlin.random.Random

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    private var mHomePageAdapter: HomePageAdapter? = null

    override fun getBindLayout(): Int = R.layout.fragment_home

    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    private val mHandler = Handler(Looper.getMainLooper()) {
        if (it.what == MSG_PLAY_SEARCH_ANIM) {
            binding.ivSearch.playAnimation()
        }

        false
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
        binding.ivSearch.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                sendPlaySearchAnimMsg()
            }
        })
        sendPlaySearchAnimMsg()
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

    /**
     * 播放搜索动画
     */
    private fun sendPlaySearchAnimMsg() {
        if (isDetached) {
            return
        }
        mHandler.removeMessages(MSG_PLAY_SEARCH_ANIM)
        //随机产品一个区间
        mHandler.sendEmptyMessageDelayed(MSG_PLAY_SEARCH_ANIM, Random.nextLong(12000, 26000))
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
        mHomePageAdapter = null
    }

    companion object {
        private const val MSG_PLAY_SEARCH_ANIM = 1068
        fun newInstance(bundle: Bundle? = null): HomeFragment {
            val fragment = HomeFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

}