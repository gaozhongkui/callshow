package com.epiphany.callshow.api

import com.epiphany.callshow.model.VideoItemInfo
import com.google.api.services.youtube.model.PlaylistItem

/**
 * 视频辅助类
 */
object YouTuBeVideoHelper {

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
                    VideoItemInfo(
                        snippet.resourceId.videoId,
                        url,
                        width,
                        height,
                        title = snippet.title
                    )
                resultList.add(videoInfo)
            }
        }
        return resultList
    }

}