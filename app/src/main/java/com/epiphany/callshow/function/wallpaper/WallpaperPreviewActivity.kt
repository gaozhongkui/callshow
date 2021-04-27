package com.epiphany.callshow.function.wallpaper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.ActivityWallpaperPreviewBinding
import com.epiphany.callshow.function.video.VideoPlayHelper
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource

class WallpaperPreviewActivity : BaseActivity<BaseViewModel, ActivityWallpaperPreviewBinding>(),
    Player.EventListener {

    //播放器实例
    private var mPlayer: SimpleExoPlayer? = null

    //视频地址
    private var mVideoPath: String? = null

    //音频地址
    private var mAudioPath: String? = null

    override fun getBindLayout(): Int = R.layout.activity_wallpaper_preview

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        intent?.apply {
            mVideoPath = getStringExtra(EXTRA_VIDEO_INFO)
            mAudioPath = getStringExtra(EXTRA_AUDIO_INFO)
        }
        initStatusBarLayout()
        initLayoutListener()
    }

    private fun initStatusBarLayout() {
        val layoutParams = binding.ivBackBut.layoutParams as FrameLayout.LayoutParams
        layoutParams.topMargin = SystemInfo.getStatusBarHeight(this)
        binding.ivBackBut.layoutParams = layoutParams
    }

    private fun initLayoutListener() {
        binding.ivBackBut.setOnClickListener {
            finish()
        }

        binding.tvApplyBut.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    /**
     * 初始化播放器
     */
    private fun initializePlayer() {
        //如果视频对象为空时，则直接返回
        if (TextUtils.isEmpty(mVideoPath) || TextUtils.isEmpty(mAudioPath)) {
            return
        }

        if (mPlayer == null) {
            val videoUri = Uri.parse(mVideoPath)
            val audioUri = Uri.parse(mAudioPath)
            val dataSourceFactory: DataSource.Factory =
                VideoPlayHelper.getDataSourceFactory(this)
            val videoMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))
            val audioMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(audioUri))
            val mergingMediaSource = MergingMediaSource(videoMediaSource, audioMediaSource)
            val loopingMediaSource = LoopingMediaSource(mergingMediaSource)
            mPlayer = SimpleExoPlayer.Builder(this).build()
            mPlayer?.apply {
                addListener(this@WallpaperPreviewActivity)
                setMediaSource(loopingMediaSource)
                prepare()
                binding.playerView.player = this
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
            }
            Player.STATE_READY -> {
                Log.d(TAG, "onPlayerStateChanged() called with: 视频准备完成，正要播放")
            }
            Player.STATE_ENDED -> {
                Log.d(TAG, "onPlayerStateChanged() called with: 视频结束")
            }
        }
        if (playbackState == Player.STATE_READY && !mPlayer!!.isPlaying) {
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


    override fun onResume() {
        super.onResume()
        initializePlayer()
        binding.playerView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mPlayer?.pause()
        releasePlayer()
    }

    companion object {
        private const val TAG = "WallpaperPreview"
        private const val EXTRA_VIDEO_INFO = "extra_video_path"
        private const val EXTRA_AUDIO_INFO = "extra_audio_path"
        fun getWallpaperPreviewIntent(cxt: Context, videoPath: String, audioPath: String): Intent {
            val intent = Intent(cxt, WallpaperPreviewActivity::class.java)
            intent.putExtra(EXTRA_VIDEO_INFO, videoPath)
            intent.putExtra(EXTRA_AUDIO_INFO, audioPath)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            return intent
        }
    }

}