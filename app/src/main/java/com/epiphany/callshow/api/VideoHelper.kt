package com.epiphany.callshow.api

import com.chaquo.python.Python
import com.epiphany.callshow.BuildConfig
import com.epiphany.callshow.database.VideoRoomManager
import com.epiphany.callshow.model.VideoRealPathInfo

/**
 * 视频辅助类
 */
object VideoHelper {
    private const val TAG = "VideoHelper"
    private var DEBUG = BuildConfig.DEBUG
    private val py by lazy {
        Python.getInstance()
    }

    private val mModuleObject by lazy {
        py.getModule("download_pornhub")
    }

    /**
     * 获取视频的真实地址
     */
    fun getVideoRealPathStr(videoPath: String, forciblyQuery: Boolean = false): VideoRealPathInfo {
        //判断非强制获取时，则优先从数据库中获取
        if (!forciblyQuery) {
            val cacheVideoRealPathInfo = VideoRoomManager.getSyncVideoRealPathInfo(videoPath)
            cacheVideoRealPathInfo?.let {
                return it
            }
        }


        val videoUrl = mModuleObject.callAttr("logPrintMsg",videoPath)
        val videoRealPathInfo = VideoRealPathInfo(videoPath, videoUrl.toString(), "")
        //插入到数据库中
        VideoRoomManager.insertVideoRealPath(videoRealPathInfo)
        return videoRealPathInfo
    }


}