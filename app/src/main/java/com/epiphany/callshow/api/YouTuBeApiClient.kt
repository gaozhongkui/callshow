package com.epiphany.callshow.api

import android.text.TextUtils
import android.util.Log
import com.chaquo.python.Python
import com.epiphany.callshow.model.VideoItemInfo
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

/**
 * 此类用于承载所有的网络请求的入口
 */
object YouTuBeApiClient {
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
    fun getVideos(playListId: String, nextPageToken: Int = 0): VideoResponse? {
        try {
            val data = mModuleObject.callAttr("ybsearch", playListId)
            Log.d(
                TAG, "getVideos() called with: data = $data"
            )
            val jsonDataStr = data.toString()
            //判断如果数据为空时，则直接返回
            if (TextUtils.isEmpty(jsonDataStr)) {
                return null
            }
            val jsonRoot = JSONObject(jsonDataStr)
            val jsonArray = jsonRoot.getJSONArray("videos")

            val videoList = mutableListOf<VideoItemInfo>()
            //遍历数据列表
            for (index in 0 until jsonArray.length()) {
                val itemObject = jsonArray.getJSONObject(index)
                val videoId = itemObject.optString("id")
                val previewPng = getVideoPreviewPng(itemObject.getJSONArray("thumbnails"))
                val title = itemObject.optString("title")
                videoList.add(
                    VideoItemInfo(
                        videoId,
                        previewPng,
                        300,
                        400,
                        Random.nextLong(1000000),
                        Random.nextLong(10000000),
                        title,
                        videoId,
                        null
                    )
                )
            }
            return VideoResponse(videoList)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取视频的预览图
     */
    private fun getVideoPreviewPng(jsonArray: JSONArray): String {
        if (jsonArray.length() == 0) {
            return ""
        }
        return jsonArray.optString(jsonArray.length() - 1)
    }


}