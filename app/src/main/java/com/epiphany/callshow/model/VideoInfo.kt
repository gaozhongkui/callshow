package com.epiphany.callshow.model

data class VideoInfo(
    val duration: String,
    val views: Long,
    val video_id: String,
    val rating: Float,
    val ratings: Int,
    val title:String,
    val url:String,
    val default_thumb:String,
    val thumb:String,
    val publish_date:String,

)
