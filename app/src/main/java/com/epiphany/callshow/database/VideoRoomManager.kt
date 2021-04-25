package com.epiphany.callshow.database

import androidx.room.Room
import com.epiphany.callshow.App
import com.epiphany.callshow.model.VideoRealPathInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 视频数据库管理类
 */
object VideoRoomManager {
    private val db =
        Room.databaseBuilder(App.getApp(), VideoDatabase::class.java, "v_d_b").build()


    /**
     * 插入视频的真实地址
     */
    fun insertVideoRealPath(videoRealPathInfo: VideoRealPathInfo) {
        GlobalScope.launch(Dispatchers.IO) {
            db.runInTransaction {
                db.videoRealPath().insert(videoRealPathInfo)
            }
        }
    }

    /**
     * 同步获取视频的真实地址
     */
    fun getSyncVideoRealPathInfo(videoId: String): VideoRealPathInfo? {
        return db.videoRealPath().getVideoRealPathInfo(videoId)
    }

    /**
     * 更新视频真实地址信息
     */
    fun updateVideoRealPathInfo(videoRealPathInfo: VideoRealPathInfo) {
        GlobalScope.launch(Dispatchers.IO) {
            db.runInTransaction {
                db.videoRealPath().update(videoRealPathInfo)
            }
        }
    }

}