package com.epiphany.callshow.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoItemInfo(
    val videoId: String,
    val previewPng: String,
    val width: Long,
    val high: Long,
    var viewCount: Long = 0,
    var likeCount: Long = 0,
    val title: String? = null,
    var videoUrl: String? = null,
    var audioUrl: String? = null,
    var realVideoUrl: String? = null
) : Parcelable
