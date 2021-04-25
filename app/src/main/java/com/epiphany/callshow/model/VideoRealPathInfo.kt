package com.epiphany.callshow.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 视频的真实地址信息
 */
@Entity(tableName = "video_real_path")
data class VideoRealPathInfo(
    @ColumnInfo(name = "video_id")
    val videoId: String,
    @ColumnInfo(name = "video_url")
    var videoUrl: String,
    @ColumnInfo(name = "audio_url")
    var audioUrl: String,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "v_id")
    var vId: Long = 0
)
