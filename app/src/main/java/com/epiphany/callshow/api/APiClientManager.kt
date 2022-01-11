package com.epiphany.callshow.api

object APiClientManager {
    //播放模式
    val VIDEO_PLAY_MODE = VideoType.YouTuBe

    /**
     * 获取指定的视频列表
     */
    fun getVideos(playListId: String, nextPageToken: Int = 1): VideoResponse? {
        return if (VIDEO_PLAY_MODE == VideoType.PornHub) {
            PronApiClient.getVideos(playListId, nextPageToken)
        } else {
            YouTuBeApiClient.getVideos(playListId, nextPageToken)
        }
    }

    enum class VideoType {
        YouTuBe, PornHub
    }

}