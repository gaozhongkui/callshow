package com.epiphany.callshow.function.home

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.epiphany.callshow.api.APiClientManager
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.model.VideoItemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoListViewModel : BaseViewModel() {
    //下一页的Index
    private var mNextPageIndex: Int = 0

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
        viewModelScope.launch(Dispatchers.IO) {
            if (TextUtils.isEmpty(mPlayListId)) {
                return@launch
            }
            val response = APiClientManager.getVideos(mPlayListId!!, mNextPageIndex) ?: return@launch
            //记录下一屏幕的数据
            mNextPageIndex = response.nextPageIndex
            isLoadDataMoreState = false
            val videos = response.items
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
        viewModelScope.launch(Dispatchers.IO) {
            if (TextUtils.isEmpty(mPlayListId)) {
                return@launch
            }
            val response = APiClientManager.getVideos(mPlayListId!!, mNextPageIndex) ?: return@launch
            //记录下一屏幕的数据
            mNextPageIndex = response.nextPageIndex
            isLoadDataMoreState = true
            val videos = response.items
            withContext(Dispatchers.Main) {
                mVideoDataList.value = videos
            }
        }
    }
}