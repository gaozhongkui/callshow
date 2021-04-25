package com.epiphany.callshow.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.epiphany.callshow.model.VideoRealPathInfo

/**
 * 视频数据库
 */
@Database(entities = [VideoRealPathInfo::class], version = 1)
abstract class VideoDatabase : RoomDatabase() {
    //获取视频真实地址数据库
    abstract fun videoRealPath(): VideoRealPathDao
}

