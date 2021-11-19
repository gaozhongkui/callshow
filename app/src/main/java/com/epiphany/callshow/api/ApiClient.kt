package com.epiphany.callshow.api

import android.util.Log
import com.chaquo.python.Python
import com.epiphany.callshow.model.PornHubVideoGroupInfo
import com.epiphany.callshow.model.VideoItemInfo


/**
 * 此类用于承载所有的网络请求的入口
 */
object ApiClient {
    private const val TAG = "ApiClient"

    private val py by lazy {
        Python.getInstance()
    }

    private val mModuleObject by lazy {
        py.getModule("main")
    }

    /**
     * 获取指定的视频列表
     */
    fun getVideos(playListId: String, nextPageToken: String? = null): VideoResponse? {
        try {
            val data = mModuleObject.callAttr("search", "beautiful")
            val groupInfo = data.toJava(PornHubVideoGroupInfo::class.java)

            val videoList = mutableListOf<VideoItemInfo>()
            groupInfo.data?.forEach { item ->
                videoList.add(
                    VideoItemInfo(
                        item.video_id,
                        item.imagePath,
                        300,
                        400,
                        0,
                        0,
                        item.title,
                        item.videoRealPath,
                        null
                    )
                )
            }
            return VideoResponse(videoList, "")

        } catch (e: Exception) {
            Log.w(TAG, "getVideos: ", e)
        }

        return null
    }


}