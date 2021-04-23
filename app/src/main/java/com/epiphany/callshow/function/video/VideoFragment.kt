package com.epiphany.callshow.function.video

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.View
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.FragmentVideoLayoutBinding
import com.epiphany.callshow.model.VideoItemInfo
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.ErrorMessageProvider

/**
 * 视频播放的Fragment
 */
class VideoFragment : BaseFragment<BaseViewModel, FragmentVideoLayoutBinding>(),
    Player.EventListener {
    private var mVideoItemInfo: VideoItemInfo? = null
    private var mPlayer: SimpleExoPlayer? = null
    private var mDrmSessionManager: DrmSessionManager? = null

    override fun getBindLayout(): Int = R.layout.fragment_video_layout

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        arguments?.apply {
            mVideoItemInfo = getParcelable<VideoItemInfo>(EXTRA_VIDEO_INFO)
        }
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

    /**
     * 初始化播放器
     */
    private fun initializePlayer() {
        if (mPlayer == null) {
            binding.playerView.setErrorMessageProvider(PlayerErrorMessageProvider())
            val uri =
                Uri.parse("https://r1---sn-i3belne6.googlevideo.com/videoplayback?expire=1619201474&ei=YrmCYL_qEYHa4gLEtYywDg&ip=156.251.163.112&id=o-AC6IX2l5UPyukQfMiRMKp7q3eUAFayA2sVrcpfr87UoZ&itag=315&aitags=133%2C134%2C135%2C136%2C160%2C242%2C243%2C244%2C247%2C278%2C298%2C299%2C302%2C303%2C308%2C315&source=youtube&requiressl=yes&mh=ht&mm=31%2C26&mn=sn-i3belne6%2Csn-oguesnzz&ms=au%2Conr&mv=m&mvi=1&pl=23&initcwndbps=36083750&vprv=1&mime=video%2Fwebm&ns=vrAscFKtF2ZyJh5nRphpZ6cF&gir=yes&clen=179142953&dur=216.849&lmt=1550225055106063&mt=1619179484&fvip=1&keepalive=yes&fexp=24001373%2C24007246&c=WEB&txp=5432432&n=kHR_d-H4A979sqTMYy&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRAIgBWnvVLBRWdfLQwqBWZA2pXk0JzUorLIk-PnZ1IcPQ0MCIDzJVulMplBZJw0u6uG6coBAck7WhIZMk5EsBI2pe6aj&sig=AOq0QJ8wRQIgSyrb4Vq15FmBaGWM9V3JhHD0tfjrbRDa91Ppf6ISmhMCIQC1skF9zf1MZq6EbACr02DZXUC-9ExPqbtcBs5lwibTsw==")
            mDrmSessionManager = DrmSessionManager.DRM_UNSUPPORTED
            val dataSourceFactory: DataSource.Factory =
                VideoPlayHelper.getDataSourceFactory(activity!!)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManager(mDrmSessionManager)
                .createMediaSource(MediaItem.fromUri(uri))
            val loopingMediaSource = LoopingMediaSource(mediaSource)
            mPlayer = SimpleExoPlayer.Builder(activity!!).build()
            mPlayer?.apply {
                addListener(this@VideoFragment)
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
            return Pair.create(0, throwable.message)
        }

    }

    companion object {
        private const val TAG = "video_tag"
        private const val EXTRA_VIDEO_INFO = "extra_video_info";
        fun newInstance(videoInfo: VideoItemInfo): VideoFragment {
            val fragment = VideoFragment()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_VIDEO_INFO, videoInfo)
            fragment.arguments = bundle
            return fragment
        }
    }
}


