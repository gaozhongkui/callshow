package com.epiphany.callshow.api


/**
 * 此类用于承载所有的网络请求的入口
 */
object ApiClient {
    private const val TAG = "ApiClient"

    //分页最大返回数
    private const val NUMBER_OF_VIDEOS_RETURNED: Long = 50

    /**
     * 获取指定的视频列表
     */
    fun getVideos(playListId: String, nextPageToken: String? = null): VideoResponse? {

        return null
    }

}