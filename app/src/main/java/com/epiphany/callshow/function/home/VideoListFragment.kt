package com.epiphany.callshow.function.home

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.constant.DEFAULT_PLAY_LIST_ID
import com.epiphany.callshow.databinding.FragmentVideoListLayoutBinding
import com.epiphany.callshow.function.web.FullWebActivity

/**
 * 视频列表页面
 */
class VideoListFragment : BaseFragment<VideoListViewModel, FragmentVideoListLayoutBinding>() {
    //播放列表ID
    private var mPlayListId: String? = null
    private var mVideoAdapter: VideoListAdapter? = null
    override fun getBindLayout(): Int = R.layout.fragment_video_list_layout

    override fun getViewModelClass(): Class<VideoListViewModel> {
        return VideoListViewModel::class.java
    }

    override fun initView() {
        initData()
        initLayout()
        initDataObserver()
        initLayoutListener()
    }

    private fun initData() {
        arguments?.apply {
            mPlayListId = getString(EXTRA_PLAY_LIST_ID, DEFAULT_PLAY_LIST_ID)
        }
    }

    private fun initLayout() {
        context?.apply {
            mVideoAdapter = VideoListAdapter(this)
            binding.recyclerView.layoutManager =
                StaggeredGridLayoutManager(SPAN_COUNT, RecyclerView.VERTICAL)
            binding.recyclerView.adapter = mVideoAdapter
        }
    }

    private fun initDataObserver() {
        mPlayListId?.apply {
            viewModel.setPlayListId(this)
        }
        //展示加载Loading布局
        binding.loadingView.visibility = View.VISIBLE
        viewModel.getVideoDataList().observe(this, {
            if (!SystemInfo.isValidActivity(activity)) {
                return@observe
            }
            //隐藏Loading布局
            binding.loadingView.visibility = View.GONE
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

        mVideoAdapter?.setVideoListListener(object : VideoListAdapter.IVideoListListener {
            override fun onItemClick(position: Int) {
                if (!SystemInfo.isValidActivity(activity)) {
                    return
                }
                mVideoAdapter?.apply {
                    FullWebActivity.launchAct(requireActivity(), getDataList()[position].videoUrl)
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        //判断没有数据时，则进行数据请求
        mVideoAdapter?.apply {
            if (itemCount <= 0) {
                viewModel.loadVideoData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mVideoAdapter?.releaseData()
        mVideoAdapter = null
    }

    companion object {
        private const val SPAN_COUNT = 1

        private const val EXTRA_PLAY_LIST_ID = "extra_play_list_id"
        fun newInstance(playListId: String): VideoListFragment {
            val fragment = VideoListFragment()
            val bundle = Bundle()
            bundle.putString(EXTRA_PLAY_LIST_ID, playListId)
            fragment.arguments = bundle
            return fragment
        }
    }

}