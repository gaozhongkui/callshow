package com.epiphany.callshow.function.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.epiphany.callshow.api.ApiClient
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.model.VideoItemInfo
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.Thumbnail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : BaseViewModel() {
    private var mNextPageToken: String? = null

    //视频数据集合
    private val mVideoDataList = MutableLiveData<List<VideoItemInfo>>()

    fun getVideoDataList(): LiveData<List<VideoItemInfo>> {
        return mVideoDataList
    }

    /**
     * 加载视频数据
     */
    fun loadVideoData() {
        GlobalScope.launch {
            val response = ApiClient.getVideos(PLAY_LIST_ID)
            if (response == null) {
                Log.d(TAG, "loadVideoData() called")
                return@launch
            }
            //记录下一屏幕的数据
            mNextPageToken = response.nextPageToken
            val items = response.items
            //转换数据格式
            val videos = convertPlaylistItemToVideoInfo(items)
            withContext(Dispatchers.Main) {
                mVideoDataList.value = videos
            }
        }
    }

    /**
     * 刷新视频数据
     */
    fun onRefreshVideoData() {

    }

    /**
     * 加载更多数据
     */
    fun onLoadMoreVideoData() {

    }

    /**
     * 转换数据格式
     */
    private fun convertPlaylistItemToVideoInfo(playlist: List<PlaylistItem>): List<VideoItemInfo> {
        val resultList = mutableListOf<VideoItemInfo>()
        for (item in playlist) {
            val thumbnails = item.snippet.thumbnails ?: continue
            var thumbnail = thumbnails.maxres
            if (thumbnail == null) {
                thumbnail = thumbnails.high
            }
            if (thumbnail == null) {
                thumbnail = thumbnails.standard
            }
            if (thumbnail == null) {
                thumbnail = thumbnails.default
            }
            thumbnail?.apply {
                val videoInfo = VideoItemInfo(url, width, height)
                resultList.add(videoInfo)
            }
        }
        return resultList
    }

    companion object {
        private const val PLAY_LIST_ID = "PLbduwZ5ABnEmprn8d5OdIjUZwZnWsXjrn"
        private const val TAG = "HomeViewModel"
    }
}