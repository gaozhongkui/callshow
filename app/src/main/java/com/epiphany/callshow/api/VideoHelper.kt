package com.epiphany.callshow.api

import com.epiphany.jextractor.YoutubeDownloader
import com.epiphany.jextractor.model.YoutubeVideo

/**
 * 视频辅助类
 */
object VideoHelper {
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


}