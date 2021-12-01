package com.epiphany.callshow.api

object APiClientManager {
    //播放模式
    val VIDEO_PLAY_MODE = VideoType.YouTuBe

    /**
     * 获取指定的视频列表
     */
    fun getVideos(playListId: String, nextPageToken: Any? = null): VideoResponse? {
        return if (VIDEO_PLAY_MODE == VideoType.PornHub) {
            PronApiClient.getVideos(playListId, kotlin.run {
                if (nextPageToken is Int) nextPageToken else 1
            })
        } else {
            YouTuBeApiClient.getVideos(playListId, kotlin.run {
                if (nextPageToken is String) {
                    nextPageToken
                } else {
                    null
                }
            })
        }

    }

    enum class VideoType {
        YouTuBe, PornHub
    }

}