package com.epiphany.callshow.function.wallpaper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceHolder

class VideoWallpaperService : WallpaperService() {
    private var mVideoWallPagerEngine: VideoWallPagerEngine? = null
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
        mVideoWallPagerEngine = VideoWallPagerEngine()
        return mVideoWallPagerEngine!!
    }

    private inner class VideoWallPagerEngine : Engine() {
        private var mMediaPlayer: MediaPlayer? = null
        private var mVideoVoiceControlReceiver: BroadcastReceiver? = null
        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)

            val intentFilter = IntentFilter(VIDEO_PARAMS_CONTROL_ACTION)
            mVideoVoiceControlReceiver = VideoVoiceControlReceiver()
            registerReceiver(mVideoVoiceControlReceiver, intentFilter)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            mMediaPlayer = MediaPlayer()
            holder?.apply {
                mMediaPlayer?.setSurface(surface)
            }

        }

        /**
         * 更改视频地址
         */
        fun onChangeVideoPath(videoPath: String, audioPath: String) {
            try {
                mMediaPlayer?.apply {
                    reset()
                    setDataSource(videoPath)
                    isLooping = true
                    setVolume(0f, 0f)
                    prepare()
                    start()
                }

            } catch (e: Throwable) {
                Log.w(TAG, "onChangeVideoPath: ", e)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            if (mMediaPlayer != null) {
                try {
                    mMediaPlayer!!.release()
                } catch (ignored: Throwable) {
                }
                mMediaPlayer = null
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {

            try {
                mMediaPlayer?.apply {
                    if (visible) {
                        start()
                    } else {
                        pause()
                    }
                }
            } catch (ignored: Throwable) {
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            unregisterReceiver(mVideoVoiceControlReceiver)
            Log.d(TAG, "onDestroy() called")
        }

        inner class VideoVoiceControlReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.getIntExtra(ACTION, -1)) {
                    ACTION_VOICE_NORMAL -> if (mMediaPlayer != null) {
                        mMediaPlayer!!.setVolume(1.0f, 1.0f)
                    }
                    ACTION_VOICE_SILENCE -> if (mMediaPlayer != null) {
                        mMediaPlayer!!.setVolume(0f, 0f)
                    }
                }
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