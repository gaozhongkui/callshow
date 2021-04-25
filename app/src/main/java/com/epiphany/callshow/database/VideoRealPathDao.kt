package com.epiphany.callshow.database

import androidx.room.*
import com.epiphany.callshow.model.VideoRealPathInfo

@Dao
interface VideoRealPathDao {
    @Insert
    fun insert(bean: VideoRealPathInfo): Long

    @Query("SELECT * from video_real_path where video_id = :videoId")
    fun getVideoRealPathInfo(videoId: String): VideoRealPathInfo?

    //更新数据
    @Update
    fun update(bean: VideoRealPathInfo)

    //删除数据
    @Delete
    fun delete(bean: VideoRealPathInfo)
}