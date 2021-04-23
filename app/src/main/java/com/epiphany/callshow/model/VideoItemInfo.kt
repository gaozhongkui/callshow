package com.epiphany.callshow.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoItemInfo(
    val previewPng: String,
    val width: Long,
    val high: Long,
    val title: String? = null,
    var videoUrl: String? = null
) : Parcelable
