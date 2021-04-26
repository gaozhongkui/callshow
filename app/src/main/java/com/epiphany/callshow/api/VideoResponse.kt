package com.epiphany.callshow.api

import com.epiphany.callshow.model.VideoItemInfo

/**
 * 视频请求对象体
 */
data class VideoResponse(val items: List<VideoItemInfo>, val nextPageToken: String? = null)