package com.epiphany.callshow.function.wallpaper

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.opengl.GLSurfaceView
import android.service.wallpaper.WallpaperService
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceHolder
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.function.video.VideoPlayHelper
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource


class VideoWallpaperService : WallpaperService() {
    private var mVideoWallPagerEngine: GLWallpaperEngine? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            TAG,
            "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId"
        )
        intent?.apply {
            val mVideoPath = getStringExtra(EXTRA_VIDEO_INFO)
            val mAudioPath = getStringExtra(EXTRA_AUDIO_INFO)
            //判断对象不为空时,进行设置
            if (!TextUtils.isEmpty(mVideoPath) && !TextUtils.isEmpty(mAudioPath)) {
                mVideoWallPagerEngine?.onChangeVideoPath(mVideoPath!!, mAudioPath!!)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreateEngine(): Engine {
        Log.d(TAG, "onCreateEngine() called")
        mVideoWallPagerEngine = GLWallpaperEngine(this)
        return mVideoWallPagerEngine!!
    }

    private inner class GLWallpaperEngine(private val context: Context) : Engine(),
        Player.EventListener {
        private var glSurfaceView: GLWallpaperSurfaceView? = null
        private var renderer: GLWallpaperRenderer? = null
        private var exoPlayer: SimpleExoPlayer? = null
        private var videoSource: MediaSource? = null
        private var mVideoPath: String? = null
        private var mAudioPath: String? = null

        init {
            setTouchEventsEnabled(false)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            createGLSurfaceView()
            val width = surfaceHolder.surfaceFrame.width()
            val height = surfaceHolder.surfaceFrame.height()
            renderer?.setScreenSize(width, height)
            startPlayer()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            stopPlayer()
            glSurfaceView?.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            renderer?.apply {
                if (visible) {
                    glSurfaceView?.onResume()
                    startPlayer()
                } else {
                    stopPlayer()
                    glSurfaceView?.onPause()
                }
            }
        }

        override fun onSurfaceChanged(
            surfaceHolder: SurfaceHolder?, format: Int,
            width: Int, height: Int
        ) {
            super.onSurfaceChanged(surfaceHolder, format, width, height)
            renderer?.setScreenSize(width, height)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            exoPlayer?.apply {
                if (playbackState == Player.STATE_READY && !isPlaying) {
                    play()
                }
            }
        }

        private fun startPlayer() {
            if (exoPlayer != null) {
                stopPlayer()
            }
            //判断路径地址为空时,则直接返回
            if (TextUtils.isEmpty(mVideoPath) || TextUtils.isEmpty(mAudioPath)) {
                return
            }

            val videoUri = Uri.parse(mVideoPath)
            val audioUri = Uri.parse(mAudioPath)
            val dataSourceFactory: DataSource.Factory =
                VideoPlayHelper.getDataSourceFactory(context)
            val videoMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri))
            val audioMediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(audioUri))
            val mergingMediaSource = MergingMediaSource(videoMediaSource, audioMediaSource)
            val loopingMediaSource = LoopingMediaSource(mergingMediaSource)
            exoPlayer = SimpleExoPlayer.Builder(context).build()
            exoPlayer?.apply {
                renderer?.setSourcePlayer(this)
                // Let we assume video has correct info in metadata, or user should fix it.
                renderer?.setVideoSizeAndRotation(
                    SystemInfo.getScreenWidth(),
                    SystemInfo.getScreenHeight(),
                    0
                )
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(this@GLWallpaperEngine)
                setMediaSource(loopingMediaSource)
                prepare()
            }

        }

        private fun stopPlayer() {
            exoPlayer?.apply {
                if (playWhenReady) {
                    playWhenReady = false
                    stop()
                }
                removeListener(this@GLWallpaperEngine)
                clearVideoSurface()
                release()
                exoPlayer = null
            }
            videoSource = null
        }

        private fun createGLSurfaceView() {
            glSurfaceView?.let {
                it.onDestroy()
                glSurfaceView = null
            }
            glSurfaceView = GLWallpaperSurfaceView(context)
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE)
            activityManager.takeIf { it is ActivityManager }?.let {
                val actManager = activityManager as ActivityManager
                val configInfo = actManager.deviceConfigurationInfo
                if (configInfo.reqGlEsVersion >= 0x30000) {
                    glSurfaceView?.setEGLContextClientVersion(3)
                    renderer = GLES30WallpaperRenderer(context)
                } else if (configInfo.reqGlEsVersion >= 0x20000) {
                    glSurfaceView?.setEGLContextClientVersion(2)
                    renderer = GLES20WallpaperRenderer(context)
                }

                glSurfaceView?.apply {
                    preserveEGLContextOnPause = true
                    setRenderer(renderer)
                    // On demand render will lead to black screen.
                    renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                }
            }
        }

        /**
         * 更改视频地址
         */
        fun onChangeVideoPath(videoPath: String, audioPath: String) {
            mVideoPath = videoPath
            mAudioPath = audioPath
            startPlayer()
        }

        private inner class GLWallpaperSurfaceView(context: Context) : GLSurfaceView(context) {
            /**
             * This is a hack. Because Android Live Wallpaper only has a Surface.
             * So we create a GLSurfaceView, and when drawing to its Surface,
             * we replace it with WallpaperEngine's Surface.
             */
            override fun getHolder(): SurfaceHolder {
                return surfaceHolder
            }

            fun onDestroy() {
                super.onDetachedFromWindow()
            }
        }

    }


    companion object {
        private const val TAG = "WallpaperService"
        private const val EXTRA_VIDEO_INFO = "extra_video_path"
        private const val EXTRA_AUDIO_INFO = "extra_audio_path"

        fun getWallpaperService(cxt: Context, videoPath: String, audioPath: String): Intent {
            val intent = Intent(cxt, VideoWallpaperService::class.java)
            intent.putExtra(EXTRA_VIDEO_INFO, videoPath)
            intent.putExtra(EXTRA_AUDIO_INFO, audioPath)
            return intent
        }
    }


}