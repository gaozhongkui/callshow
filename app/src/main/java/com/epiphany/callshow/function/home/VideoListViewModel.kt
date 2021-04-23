package com.epiphany.callshow.function.home

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.epiphany.callshow.api.ApiClient
import com.epiphany.callshow.api.VideoHelper
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.model.VideoItemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoListViewModel : BaseViewModel() {
    //下一页的Token
    private var mNextPageToken: String? = null

    //加载更多数据的状态
    private var isLoadDataMoreState = false

    private var mPlayListId: String? = null

    //视频数据集合
    private val mVideoDataList = MutableLiveData<List<VideoItemInfo>>()

    fun getVideoDataList(): LiveData<List<VideoItemInfo>> {
        return mVideoDataList
    }

    /**
     * 加载更多数据的状态
     */
    fun isLoadDataMoreState(): Boolean {
        return isLoadDataMoreState
    }

    /**
     * 设置播放列表ID
     */
    fun setPlayListId(playListId: String) {
        mPlayListId = playListId
    }

    /**
     * 加载视频数据
     */
    fun loadVideoData() {
        GlobalScope.launch {
            if (TextUtils.isEmpty(mPlayListId)) {
                return@launch
            }
            val response = ApiClient.getVideos(mPlayListId!!)
            if (response == null) {
                Log.d(TAG, "loadVideoData() called")
                return@launch
            }
            //记录下一屏幕的数据
            mNextPageToken = response.nextPageToken
            isLoadDataMoreState = false
            val items = response.items
            //转换数据格式
            val videos = VideoHelper.convertPlaylistItemToVideoInfo(items)
            withContext(Dispatchers.Main) {
                mVideoDataList.value = videos
            }
        }
    }

    /**
     * 刷新视频数据
     */
    fun onRefreshVideoData() {
        loadVideoData()
    }

    /**
     * 加载更多数据
     */
    fun onLoadMoreVideoData() {
        GlobalScope.launch {
            if (TextUtils.isEmpty(mPlayListId)) {
                return@launch
            }
            val response = ApiClient.getVideos(mPlayListId!!, mNextPageToken)
            if (response == null) {
                Log.d(TAG, "loadVideoData() called")
                return@launch
            }
            //记录下一屏幕的数据
            mNextPageToken = response.nextPageToken
            isLoadDataMoreState = true
            val items = response.items
            //转换数据格式
            val videos = VideoHelper.convertPlaylistItemToVideoInfo(items)
            withContext(Dispatchers.Main) {
                mVideoDataList.value = videos
            }
        }
    }

    companion object {
        private const val TAG = "VideoListViewModel"
    }
}