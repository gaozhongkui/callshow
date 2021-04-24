package com.epiphany.callshow.api

import android.util.Log
import com.epiphany.callshow.BuildConfig
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.model.VideoItemInfo
import com.epiphany.callshow.model.VideoRealPathInfo
import com.epiphany.jextractor.YoutubeDownloader
import com.epiphany.jextractor.model.YoutubeVideo
import com.epiphany.jextractor.model.formats.Format
import com.epiphany.jextractor.model.formats.VideoFormat
import com.epiphany.jextractor.model.quality.AudioQuality
import com.google.api.services.youtube.model.PlaylistItem
import kotlin.math.abs

/**
 * 视频辅助类
 */
object VideoHelper {
    private const val TAG = "VideoHelper"
    private var DEBUG = BuildConfig.DEBUG
    private val mDownloader = YoutubeDownloader()

    init {
        mDownloader.setParserRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36"
        )
        mDownloader.setParserRetryOnFailure(1);
    }

    /**
     * 获取视频的真实地址
     */
    fun getVideoRealPath(videoId: String): YoutubeVideo {
        return mDownloader.getVideo(videoId)
    }

    /**
     * 获取视频的真实地址
     */
    fun getVideoRealPathStr(videoId: String): VideoRealPathInfo {
        val videoInfo = getVideoRealPath(videoId)
        var audioPath = videoInfo.audioFormats()[0].url()
        //遍历音频文件类型
        for (audioFormat in videoInfo.audioFormats()) {
            if (audioFormat.audioQuality() == AudioQuality.medium) {
                audioPath = audioFormat.url()
                break
            }
        }

        val firstVideoFormat = videoInfo.formats()[1]
        val screenHeight = SystemInfo.getScreenHeight()
        var distance = if (firstVideoFormat is VideoFormat) {
            abs(firstVideoFormat.height() - screenHeight)
        } else 0
        var deskVideoFormat: Format? = null
        //遍历视频类型,取一个最接近屏幕高度的视频
        for (videoFormat in videoInfo.formats()) {
            //判断非视频模式的，跳下一个
            if (!videoFormat.extension().isVideo) {
                continue
            }

            if (videoFormat is VideoFormat) {
                val cDistance: Int = abs(videoFormat.height() - screenHeight)
                Log.d(
                    TAG,
                    "getVideoRealPathStr() called with: height = ${videoFormat.height()}"
                )
                if (cDistance < distance) {
                    deskVideoFormat = videoFormat
                    distance = cDistance
                }
            }
        }

        //判断如果为空时，则设置一个默认的
        if (deskVideoFormat == null) {
            //遍历视频类型,取一个视频类型
            for (videoFormat in videoInfo.formats()) {
                //判断非视频模式的，跳下一个
                if (!videoFormat.extension().isVideo) {
                    continue
                }
                if (videoFormat is VideoFormat) {
                    deskVideoFormat = videoFormat
                    break
                }
            }
        }
        if (DEBUG) {
            Log.d(
                TAG,
                "getVideoRealPathStr() called with: screenHeight = $screenHeight  deskVideoFormat:${(deskVideoFormat as VideoFormat).height()}"
            )
        }
        val videoUrl = deskVideoFormat!!.url()

        return VideoRealPathInfo(videoUrl, audioPath)
    }

    /**
     * 转换数据格式
     */
    fun convertPlaylistItemToVideoInfo(playlist: List<PlaylistItem>): List<VideoItemInfo> {
        val resultList = mutableListOf<VideoItemInfo>()
        for (item in playlist) {
            val snippet = item.snippet
            val thumbnails = snippet.thumbnails ?: continue
            var thumbnail = thumbnails.maxres
            if (thumbnail == null) {
                thumbnail = thumbnails.high
            }
            if (thumbnail == null) {
                thumbnail = thumbnails.standard
            }
            if (thumbnail == null) {
                thumbnail = thumbnails.default
            }
            thumbnail?.apply {
                val videoInfo =
                    VideoItemInfo(snippet.resourceId.videoId, url, width, height, snippet.title)
                resultList.add(videoInfo)
            }
        }
        return resultList
    }

}