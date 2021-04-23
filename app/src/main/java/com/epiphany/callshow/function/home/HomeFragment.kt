package com.epiphany.callshow.function.home

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
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
            mVideoAdapter?.setDataList(it)
        })
        viewModel.loadVideoData()
    }

    private fun initLayoutListener() {

    }

    private companion object {
        private const val SPAN_COUNT = 2
    }

}