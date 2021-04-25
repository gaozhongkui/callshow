package com.epiphany.callshow.function.video

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.epiphany.callshow.R
import com.epiphany.callshow.api.VideoHelper.getVideoRealPathStr
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.FragmentVideoLayoutBinding
import com.epiphany.callshow.model.VideoItemInfo
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.drm.DrmSessionManager
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
    private var mDrmSessionManager: DrmSessionManager? = null

    //是否加载真实的路径
    private var isLoadingVideoRealPath = AtomicBoolean(false)

    override fun getBindLayout(): Int = R.layout.fragment_video_layout

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        arguments?.apply {
            mVideoItemInfo = getParcelable<VideoItemInfo>(EXTRA_VIDEO_INFO)
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
    }

    /**
     * 加载真实的路径地址
     */
    private fun loadVideoRealPath() {
        GlobalScope.launch {
            mVideoItemInfo?.apply {
                val videoRealInfo = getVideoRealPathStr(videoId)
                withContext(Dispatchers.Main) {
                    videoUrl = videoRealInfo.videoUrl
                    audioUrl = videoRealInfo.audioUrl
                    //设置为非加载中的状态
                    isLoadingVideoRealPath.set(false)
                    //判断当前页面不可见时，则直接返回
                    if (!isResumed || !isAdded) {
                        return@withContext
                    }
                    initializePlayer()
                }
            }

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
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.ivPlaceholder)
        }
    }

    override fun onResume() {
        super.onResume()
        showPlaceholderView()
        initializePlayer()
        binding.playerView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //恢复默认状态
        binding.ivPlaceholder.visibility = View.VISIBLE
        mPlayer?.pause()
        releasePlayer()
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
            mDrmSessionManager = DrmSessionManager.DRM_UNSUPPORTED
            val dataSourceFactory: DataSource.Factory =
                VideoPlayHelper.getDataSourceFactory(activity!!)
            val videoMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManager(mDrmSessionManager)
                .createMediaSource(MediaItem.fromUri(videoUri))
            val audioMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManager(mDrmSessionManager)
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
        mDrmSessionManager?.release()
        mDrmSessionManager = null
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
                        // TODO: 2021/4/25 编写视频地址不存在的逻辑
                    }
                }
            }
            return Pair.create(0, throwable.message)
        }

    }

    companion object {
        private const val TAG = "video_tag"
        private const val EXTRA_VIDEO_INFO = "extra_video_info"
        fun newInstance(videoInfo: VideoItemInfo): VideoFragment {
            val fragment = VideoFragment()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_VIDEO_INFO, videoInfo)
            fragment.arguments = bundle
            return fragment
        }
    }
}


