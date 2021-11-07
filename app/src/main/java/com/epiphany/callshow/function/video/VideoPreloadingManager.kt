package com.epiphany.callshow.function.video

import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import com.epiphany.callshow.App
import com.epiphany.callshow.model.VideoItemInfo
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter

/**
 * 视频预加载管理类
 */
class VideoPreloadingManager {
    //预加载过的视频列表
    private val mPreloadedVideoList = mutableListOf<VideoItemInfo>()
    private val mHandlerThread = HandlerThread("video_preloading")
    private var mHandler: Handler

    //当前加载视频信息
    private var mCurrentPreloadVideoInfo: VideoItemInfo? = null

    //当前正在缓存的对象
    private var mCurrentCacheWriter: CacheWriter? = null

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper) { msg ->
            if (msg.what == MSG_VIDEO_PRELOADING) {
                doPreloadingVideoAction()
            }
            false
        }
    }

    /**
     * 预加载视频
     */
    fun preloadingVideo(videoItemInfo: VideoItemInfo) {
        //判断如果已经存在了，则直接返回
        if (mPreloadedVideoList.contains(videoItemInfo)) {
            return
        }
        mPreloadedVideoList.add(0, videoItemInfo)
        //判断如果不存在时，则发起一个通知
        if (!mHandler.hasMessages(MSG_VIDEO_PRELOADING)) {
            mHandler.sendEmptyMessage(MSG_VIDEO_PRELOADING)
        }
    }

    /**
     * 取消预加载
     */
    fun cancelPreloading(videoItemInfo: VideoItemInfo) {
        mPreloadedVideoList.remove(videoItemInfo)

        mCurrentPreloadVideoInfo?.apply {
            //判断如果当前正在执行的缓存正式取消的对象，则立马取消缓存
            if (TextUtils.equals(videoId, videoItemInfo.videoId)) {
                try {
                    mCurrentCacheWriter?.cancel()
                } catch (e: Throwable) {
                }
                mCurrentCacheWriter = null
                mCurrentPreloadVideoInfo = null
            }
        }
    }

    /**
     * 执行预加载的逻辑
     */
    private fun doPreloadingVideoAction() {
        if (mPreloadedVideoList.isEmpty()) {
            return
        }
        val videoInfo = mPreloadedVideoList.first()
        //判断如果路径为空时，则获取路径
        if (TextUtils.isEmpty(videoInfo.videoUrl)) {
           /* val videoRealPathInfo = VideoHelper.getVideoRealPathStr(videoInfo.videoId)
            videoInfo.videoUrl = videoRealPathInfo.videoUrl
            videoInfo.audioUrl = videoRealPathInfo.videoUrl*/
        }
        doCacheAction(videoInfo)
        //移除已经在下载的数据条目
        mPreloadedVideoList.remove(videoInfo)
    }

    /**
     * 执行缓存操作
     */
    private fun doCacheAction(info: VideoItemInfo) {
        mCurrentPreloadVideoInfo = info
        val videoUri = Uri.parse(info.videoUrl)
        val dataSource: CacheDataSource =
            VideoPlayHelper.getDataSourceFactory(App.getApp()).createDataSource() as CacheDataSource
        val dataSpec = DataSpec(videoUri, 0, 8 * 1024 * 1024)//8M
        mCurrentCacheWriter = CacheWriter(
            dataSource,
            dataSpec,
            true,
            null
        ) { requestLength, bytesCached, newBytesCached ->
            Log.d(
                TAG,
                "onProgress() called with: requestLength = $requestLength, bytesCached = $bytesCached, newBytesCached = $newBytesCached"
            )
        }
        try {
            mCurrentCacheWriter?.cache()
        } catch (e: Throwable) {

        }

    }

    /**
     * 释放资源
     */
    private fun releaseData() {
        mHandler.removeCallbacksAndMessages(null)
        mHandlerThread.quit()
    }


    companion object {
        private const val TAG = "VideoPreloadingManager"
        private const val MSG_VIDEO_PRELOADING = 1
        private val mPreloadingManager by lazy {
            VideoPreloadingManager()
        }

        fun getInstance(): VideoPreloadingManager {
            synchronized(VideoPreloadingManager::class) {
                return mPreloadingManager
            }
        }

        fun releaseData() {
            mPreloadingManager.releaseData()
        }
    }
}