package com.epiphany.callshow.function.video

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.epiphany.callshow.R
import com.epiphany.callshow.api.VideoHelper.getVideoRealPathStr
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.common.utils.DownloadHelper
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentVideoLayoutBinding
import com.epiphany.callshow.dialog.DialogUtil
import com.epiphany.callshow.dialog.SettingFunProgressDialog
import com.epiphany.callshow.function.callshow.CallShowDisplayActivity
import com.epiphany.callshow.function.wallpaper.WallpaperHelper
import com.epiphany.callshow.model.VideoItemInfo
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.ErrorMessageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 视频播放的Fragment
 */
class VideoFragment : BaseFragment<BaseViewModel, FragmentVideoLayoutBinding>(),
    Player.EventListener {
    //视频的信息
    private var mVideoItemInfo: VideoItemInfo? = null

    //播放器实例
    private var mPlayer: SimpleExoPlayer? = null

    //是否加载真实的路径
    private var isLoadingVideoRealPath = AtomicBoolean(false)

    //判断是否执行设置壁纸动作
    private var isRunSettingWallpaperAction = AtomicBoolean(false)

    //设置进度弹框
    private var mSettingFunProgressDialog: SettingFunProgressDialog? = null

    override fun getBindLayout(): Int = R.layout.fragment_video_layout

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        arguments?.apply {
            setBottomLayoutState(getBoolean(EXTRA_IS_VIDEO_DETAILS, false))
            setControlViewDisplayState(getBoolean(EXTRA_IS_SHOW_CONTROL_VIEW, true))
            mVideoItemInfo = getParcelable(EXTRA_VIDEO_INFO)
            mVideoItemInfo?.apply {
                binding.loadingView.visibility = View.VISIBLE
                showPlaceholderView()
                binding.tvTitle.text = title
                //判断如果视频的地址为空，则进行加载
                if (TextUtils.isEmpty(videoUrl)) {
                    isLoadingVideoRealPath.set(true)
                    loadVideoRealPath()
                } else {
                    isLoadingVideoRealPath.set(false)
                }
            }
        }
        initLayoutListener()
    }

    private fun initLayoutListener() {

        binding.llSettingWallpaper.setOnClickListener {
            if (!SystemInfo.isValidActivity(activity)) {
                return@setOnClickListener
            }
            setVideoWallpaper()
        }

        binding.llPreview.setOnClickListener {
            if (!SystemInfo.isValidActivity(activity)) {
                return@setOnClickListener
            }
            CallShowDisplayActivity.launchActivity(activity!!, mVideoItemInfo)
        }
    }

    fun setVideoWallpaper() {
        if (mVideoItemInfo == null || !SystemInfo.isValidActivity(activity)) {
            return
        }
        showLoadingDialog()

        //判断如果真实地址正在加载中,则设置状态后,直接返回
        if (isLoadingVideoRealPath.get()) {
            isRunSettingWallpaperAction.set(true)
            return
        }
        isRunSettingWallpaperAction.set(false)
        DownloadHelper.downloadVideo(activity!!, mVideoItemInfo!!, object :
            DownloadHelper.IDownloadMangerListener {
            override fun onDownloadComplete(videoUrl: String, audioUrl: String) {
                hideLoadingDialog()
                if (SystemInfo.isValidActivity(activity)) {
                    WallpaperHelper.setToWallPaper(activity!!, videoUrl, audioUrl)
                }
            }

            override fun onDownloadFailed(reason: String) {
                hideLoadingDialog()
            }

        })

    }


    /**
     * 展示Loading弹框
     */
    private fun showLoadingDialog() {
        if (!SystemInfo.isValidActivity(activity)) {
            return
        }
        mSettingFunProgressDialog?.apply {
            DialogUtil.dismissDialog(this)
        }
        mSettingFunProgressDialog = SettingFunProgressDialog.showLoadingDialog(activity!!)
    }

    /**
     * 隐藏弹框展示
     */
    private fun hideLoadingDialog() {
        mSettingFunProgressDialog?.let {
            DialogUtil.dismissDialog(it)
        }
    }

    /**
     * 显示控制布局的展示状态
     */
    private fun setControlViewDisplayState(isShow: Boolean) {
        Log.d(TAG, "setControlViewDisplayState() called with: isShow = $isShow")
        if (isShow) {
            binding.tvSettingCallShow.visibility = View.VISIBLE
            binding.llSettingWallpaper.visibility = View.VISIBLE
            binding.llPreview.visibility = View.VISIBLE
            binding.tvTitle.visibility = View.VISIBLE
        } else {
            binding.tvSettingCallShow.visibility = View.GONE
            binding.llSettingWallpaper.visibility = View.GONE
            binding.llPreview.visibility = View.GONE
            binding.tvTitle.visibility = View.GONE
        }
    }


    private fun setBottomLayoutState(isVideoDetails: Boolean) {
        if (isVideoDetails) {
            val layoutParams = binding.bottomView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.bottomMargin = 0
            binding.bottomView.layoutParams = layoutParams
        }
    }

    /**
     * 加载真实的路径地址
     */
    private fun loadVideoRealPath(forciblyQuery: Boolean = false) {
        GlobalScope.launch {
            mVideoItemInfo?.apply {
                val videoRealInfo = getVideoRealPathStr(videoId, forciblyQuery)
                withContext(Dispatchers.Main) {
                    videoUrl = videoRealInfo.videoUrl
                    audioUrl = videoRealInfo.audioUrl
                    //设置为非加载中的状态
                    isLoadingVideoRealPath.set(false)
                    //判断当前页面不可见时，则直接返回
                    if (!isResumed || !isAdded) {
                        return@withContext
                    }
                    //判断如果执行设置壁纸时,则执行这个动作
                    if (isRunSettingWallpaperAction.get()) {
                        setVideoWallpaper()
                    }
                    initializePlayer()
                }
            }

        }
    }

    /**
     * 预下载下一条视频
     */
    private fun preloadingNextVideo() {
        if (parentFragment is VideoDisplayFragment) {
            (parentFragment as VideoDisplayFragment).preloadingNextVideo()
        } else if (activity is VideoDetailsActivity) {
            (activity as VideoDetailsActivity).preloadingNextVideo()
        }
    }

    /**
     * 显示展位图布局
     */
    private fun showPlaceholderView() {
        binding.ivPlaceholder.visibility = View.VISIBLE
        mVideoItemInfo?.apply {
            Glide.with(this@VideoFragment).load(previewPng)
                .placeholder(R.drawable.bg_video_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.ivPlaceholder)
        }
    }

    override fun onResume() {
        super.onResume()
        showPlaceholderView()
        initializePlayer()
        binding.playerView.onResume()
        cancelPreloading()
    }

    override fun onPause() {
        super.onPause()
        //恢复默认状态
        binding.ivPlaceholder.visibility = View.VISIBLE
        mPlayer?.pause()
        releasePlayer()
        cancelPreloading()
    }

    /**
     * 取消预加载
     */
    private fun cancelPreloading() {
        mVideoItemInfo?.apply {
            VideoPreloadingManager.getInstance().cancelPreloading(this)
        }
    }

    /**
     * 初始化播放器
     */
    private fun initializePlayer() {
        //如果视频对象为空时，则直接返回
        if (mVideoItemInfo == null) {
            return
        }
        //判断如果正在加载真实的路径地址时，则直接返回
        if (isLoadingVideoRealPath.get()) {
            return
        }
        if (mPlayer == null) {
            binding.playerView.setErrorMessageProvider(PlayerErrorMessageProvider())
            val videoUri = Uri.parse(mVideoItemInfo?.videoUrl)
            val audioUri = Uri.parse(mVideoItemInfo?.audioUrl)
            val dataSourceFactory: DataSource.Factory =
                VideoPlayHelper.getDataSourceFactory(activity!!)
            val videoMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))
            val audioMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(audioUri))
            val mergingMediaSource = MergingMediaSource(videoMediaSource, audioMediaSource)
            val loopingMediaSource = LoopingMediaSource(mergingMediaSource)
            mPlayer = SimpleExoPlayer.Builder(activity!!).build()
            mPlayer?.apply {
                addListener(this@VideoFragment)
                setMediaSource(loopingMediaSource)
                prepare()
                binding.playerView.player = this
                setVideoScaleType()
            }
        }
    }

    /**
     * 设置视频的缩放模式
     */
    private fun setVideoScaleType() {
        val screenHeight = SystemInfo.getScreenHeight()
        val screenWidth = SystemInfo.getScreenWidth()

        mVideoItemInfo?.apply {
            //判断如果宽度小于屏幕，则直接铺满全屏
            if (width < screenWidth || high < screenHeight) {
                binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            } else {
                binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
            }
        }

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)

        when (playbackState) {
            Player.STATE_IDLE -> {
                Log.d(TAG, "onPlayerStateChanged() called with: Player.STATE_IDLE")
            }
            Player.STATE_BUFFERING -> {
                Log.d(TAG, "onPlayerStateChanged() called with: 视频缓存中")
                binding.loadingView.visibility = View.VISIBLE
            }
            Player.STATE_READY -> {
                Log.d(
                    TAG,
                    "onPlayerStateChanged() called with: 视频准备完成，正要播放 playWhenReady:$playWhenReady"
                )

                Log.d(
                    TAG,
                    "videoID = ${mVideoItemInfo?.videoId}, videoUrl = ${mVideoItemInfo?.videoUrl}"
                )
                //判断当前是可见状态时，则设置为不可见
                if (isResumed && playWhenReady) {
                    preloadingNextVideo()
                    binding.ivPlaceholder.visibility = View.GONE
                    binding.loadingView.visibility = View.GONE
                }
            }
            Player.STATE_ENDED -> {
                Log.d(TAG, "onPlayerStateChanged() called with: 视频结束")
            }
        }
        if (playbackState == Player.STATE_READY && !mPlayer!!.isPlaying && isResumed) {
            mPlayer?.play()
        }
    }

    /**
     * 回收播放器的资源
     */
    private fun releasePlayer() {
        mPlayer?.removeListener(this)
        mPlayer?.clearVideoSurface()
        mPlayer?.release()
        mPlayer = null
    }

    private inner class PlayerErrorMessageProvider : ErrorMessageProvider<ExoPlaybackException> {
        override fun getErrorMessage(throwable: ExoPlaybackException): Pair<Int, String> {
            Log.w(TAG, "getErrorMessage: ", throwable)
            throwable.cause?.apply {
                if (this is HttpDataSource.InvalidResponseCodeException) {
                    //视频地址不存在
                    if (responseCode == 403) {
                        releasePlayer()
                        loadVideoRealPath(true)
                    }
                }
            }
            return Pair.create(0, throwable.message)
        }

    }

    companion object {
        private const val TAG = "video_tag"
        private const val EXTRA_VIDEO_INFO = "extra_video_info"
        private const val EXTRA_IS_VIDEO_DETAILS = "extra_is_video_details"
        private const val EXTRA_IS_SHOW_CONTROL_VIEW = "extra_is_show_control_view"
        fun newInstance(
            videoInfo: VideoItemInfo,
            isVideoDetails: Boolean = false,
            isShowControlView: Boolean = true
        ): VideoFragment {
            val fragment = VideoFragment()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_VIDEO_INFO, videoInfo)
            bundle.putBoolean(EXTRA_IS_VIDEO_DETAILS, isVideoDetails)
            bundle.putBoolean(EXTRA_IS_SHOW_CONTROL_VIEW, isShowControlView)
            fragment.arguments = bundle
            return fragment
        }
    }
}


