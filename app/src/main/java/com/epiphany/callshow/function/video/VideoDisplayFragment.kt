package com.epiphany.callshow.function.video

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentVideoDisplayBinding
import com.epiphany.callshow.function.home.HomeFragment
import com.epiphany.callshow.function.home.HomeVideoAdapter

class VideoDisplayFragment : BaseFragment<VideoDisplayViewModel, FragmentVideoDisplayBinding>() {
    private var mVideoDisplayAdapter: VideoDisplayAdapter? = null
    override fun getBindLayout(): Int = R.layout.fragment_video_display

    override fun getViewModelClass(): Class<VideoDisplayViewModel> {
        return VideoDisplayViewModel::class.java
    }

    override fun initView() {
        initLayout()
        initDataObserver()
        initLayoutListener()

    }

    @SuppressLint("WrongConstant")
    private fun initLayout() {
        context?.apply {
            mVideoDisplayAdapter = VideoDisplayAdapter(this@VideoDisplayFragment)
            binding.viewPager.adapter = mVideoDisplayAdapter
            binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            binding.viewPager.offscreenPageLimit = VIDEO_OFFSCREEN_PAGE_LIMIT
        }
    }

    private fun initDataObserver() {
        viewModel.getVideoDataList().observe(this, {
            if (!SystemInfo.isValidActivity(activity)) {
                return@observe
            }
            val isRefresh = !viewModel.isLoadDataMoreState()
            if (binding.smartRefresh.isRefreshing) {
                binding.smartRefresh.finishRefresh()
            }
            mVideoDisplayAdapter?.setDataList(it, isRefresh)
            //判断如果为刷新状态时，则回到顶部
            if (isRefresh) {
                binding.viewPager.currentItem = 0
            }
            if (binding.smartRefresh.isLoading) {
                binding.smartRefresh.finishLoadMore()
            }
        })
        viewModel.loadVideoData()
    }

    private fun initLayoutListener() {
        binding.smartRefresh.setOnRefreshListener {
            if (!SystemInfo.isValidActivity(activity)) {
                return@setOnRefreshListener
            }
            viewModel.onRefreshVideoData()
        }
        binding.smartRefresh.setOnLoadMoreListener {
            if (!SystemInfo.isValidActivity(activity)) {
                return@setOnLoadMoreListener
            }
            viewModel.onLoadMoreVideoData()
        }
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //判断为第一个时，才可以滑动
                binding.smartRefresh.setEnableRefresh(position == 0)
            }
        })
    }

    companion object {
        private const val VIDEO_OFFSCREEN_PAGE_LIMIT = 1
        fun newInstance(bundle: Bundle? = null): VideoDisplayFragment {
            val fragment = VideoDisplayFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}