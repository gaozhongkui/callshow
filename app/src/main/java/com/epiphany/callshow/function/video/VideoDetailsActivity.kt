package com.epiphany.callshow.function.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.ActivityVideoDetailsBinding
import com.epiphany.callshow.model.VideoItemInfo

class VideoDetailsActivity : BaseActivity<VideoDetailsViewModel, ActivityVideoDetailsBinding>() {
    private var mVideoDetailsAdapter: VideoDetailsAdapter? = null
    override fun getBindLayout(): Int = R.layout.activity_video_details

    override fun getViewModelClass(): Class<VideoDetailsViewModel> {
        return VideoDetailsViewModel::class.java
    }

    override fun initView() {
        initLayout()
        initLayoutData()
        initDataObserver()
        initLayoutListener()
    }

    private fun initLayoutData() {
        intent?.apply {
            val dataList =
                getParcelableArrayListExtra<VideoItemInfo>(EXTRA_VIDEO_DATA_LIST)
            dataList?.apply {
                mVideoDetailsAdapter?.setDataList(this, true)
            }
            val currentPosition = getIntExtra(EXTRA_CURRENT_POSITION, 0)
            val playListId = getStringExtra(EXTRA_PLAY_LIST_ID)
            val nextPageToken = getStringExtra(EXTRA_NEXT_PAGE_TOKEN)
            playListId?.apply {
                viewModel.setPlayListId(playListId)
            }
            viewModel.setNextPageToken(nextPageToken)
            binding.viewPager.setCurrentItem(currentPosition, false)
        }
    }

    @SuppressLint("WrongConstant")
    private fun initLayout() {
        mVideoDetailsAdapter = VideoDetailsAdapter(this)
        binding.viewPager.adapter = mVideoDetailsAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPager.offscreenPageLimit = VIDEO_OFFSCREEN_PAGE_LIMIT
        initStatusBarLayout()
    }

    private fun initStatusBarLayout() {
        val layoutParams = binding.ivBackBut.layoutParams as FrameLayout.LayoutParams
        layoutParams.topMargin = SystemInfo.getStatusBarHeight(this)
        binding.ivBackBut.layoutParams = layoutParams
    }


    private fun initDataObserver() {
        viewModel.getVideoDataList().observe(this, {
            if (!SystemInfo.isValidActivity(this)) {
                return@observe
            }
            val isRefresh = !viewModel.isLoadDataMoreState()
            if (binding.smartRefresh.isRefreshing) {
                binding.smartRefresh.finishRefresh()
            }
            mVideoDetailsAdapter?.setDataList(it, isRefresh)
            //判断如果为刷新状态时，则回到顶部
            if (isRefresh) {
                binding.viewPager.currentItem = 0
            }
            if (binding.smartRefresh.isLoading) {
                binding.smartRefresh.finishLoadMore()
            }
        })
    }

    private fun initLayoutListener() {
        binding.smartRefresh.setOnRefreshListener {
            if (!SystemInfo.isValidActivity(this)) {
                return@setOnRefreshListener
            }
            viewModel.onRefreshVideoData()
        }
        binding.smartRefresh.setOnLoadMoreListener {
            if (!SystemInfo.isValidActivity(this)) {
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
        binding.ivBackBut.setOnClickListener { onBackPressed() }
    }

    /**
     * 预下载下一条视频
     */
    fun preloadingNextVideo() {
        val nextItem = binding.viewPager.currentItem + 1
        mVideoDetailsAdapter?.getItemData(nextItem)?.apply {
            VideoPreloadingManager.getInstance().preloadingVideo(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mVideoDetailsAdapter?.releaseData()
    }

    companion object {
        private const val VIDEO_OFFSCREEN_PAGE_LIMIT = 1
        private const val EXTRA_CURRENT_POSITION = "extra_current_position"
        private const val EXTRA_VIDEO_DATA_LIST = "extra_video_data_list"
        private const val EXTRA_NEXT_PAGE_TOKEN = "extra_next_page_token"
        private const val EXTRA_PLAY_LIST_ID = "extra_play_list_id"
        fun launchActivity(
            cxt: Context,
            currentPosition: Int,
            dataList: ArrayList<VideoItemInfo>,
            playListId: String,
            nextPageToken: String? = null
        ) {
            val intent = Intent(cxt, VideoDetailsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(EXTRA_CURRENT_POSITION, currentPosition)
            intent.putExtra(EXTRA_NEXT_PAGE_TOKEN, nextPageToken)
            intent.putExtra(EXTRA_PLAY_LIST_ID, playListId)
            intent.putParcelableArrayListExtra(EXTRA_VIDEO_DATA_LIST, dataList)
            cxt.startActivity(intent)
        }
    }
}