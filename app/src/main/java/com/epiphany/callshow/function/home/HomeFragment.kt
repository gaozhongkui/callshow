package com.epiphany.callshow.function.home

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<HomeViewModel, FragmentHomeBinding>() {
    private var mVideoAdapter: HomeVideoAdapter? = null
    override fun getBindLayout(): Int = R.layout.fragment_home

    override fun getViewModelClass(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun initView() {
        initLayout()
        initDataObserver()
        initLayoutListener()
    }

    private fun initLayout() {
        context?.apply {
            mVideoAdapter = HomeVideoAdapter(this)
            binding.recyclerView.layoutManager =
                StaggeredGridLayoutManager(SPAN_COUNT, RecyclerView.VERTICAL)
            binding.recyclerView.adapter = mVideoAdapter
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
            mVideoAdapter?.setDataList(it, isRefresh)
            //判断如果为刷新状态时，则回到顶部
            if (isRefresh) {
                binding.recyclerView.smoothScrollToPosition(0)
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
    }

    private companion object {
        private const val SPAN_COUNT = 2
    }

}