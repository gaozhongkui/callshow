package com.epiphany.callshow.model

data class VideoItemInfo(
    val previewPng: String,
    val width: Long,
    val high: Long,
    var videoUrl: String? = null
)