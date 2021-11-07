package com.epiphany.callshow.api

import android.util.Log
import com.chaquo.python.Python


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
            Log.d(TAG, "getVideos() called with: data = $data")

            val videos = data["videos"]
            Log.d(TAG, "getVideos() called with: videos.length = ${videos}")

            videos?.let {
                val resultList = it.toList()
                Log.d(TAG, "getVideos() called with: resultList = ${resultList.toString()}")
                it.values.forEach {
                    Log.d(TAG, "getVideos() called with: it = ${it.toString()}")
                }


                /* videos.values.forEach {
                     Log.d(TAG, "getVideos() called with: video = ${it}")
                 }*/
            }

        } catch (e: Exception) {
            Log.w(TAG, "getVideos: ", e)
        }

        return null
    }


}