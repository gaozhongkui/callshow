package com.epiphany.callshow.api

import android.util.Log
import com.chaquo.python.Python
import com.epiphany.callshow.model.PornHubVideoGroupInfo
import com.epiphany.callshow.model.VideoItemInfo
import kotlin.random.Random


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
    fun getVideos(playListId: String, nextPage: Int = 1): VideoResponse? {
        try {
            val data = mModuleObject.callAttr("search", playListId, nextPage)
            val groupInfo = data.toJava(PornHubVideoGroupInfo::class.java)

            val videoList = mutableListOf<VideoItemInfo>()
            groupInfo.data?.forEach { item ->
                videoList.add(
                    VideoItemInfo(
                        item.video_id,
                        item.imagePath,
                        300,
                        400,
                        Random.nextLong(1000000),
                        Random.nextLong(10000000),
                        item.title,
                        item.videoRealPath,
                        null
                    )
                )
            }
            return VideoResponse(videoList, nextPage + 1)

        } catch (e: Exception) {
            Log.w(TAG, "getVideos: ", e)
        }

        return null
    }


}