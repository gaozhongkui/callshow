package com.epiphany.callshow.function.home

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.epiphany.callshow.api.ApiClient
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.model.VideoItemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoListViewModel : BaseViewModel() {
    //下一页的Index
    private var mNextPageIndex: Int = 1

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

    fun getNextPageInt(): Int {
        return mNextPageIndex
    }

    /**
     * 加载视频数据
     */
    fun loadVideoData() {
        GlobalScope.launch {
            if (TextUtils.isEmpty(mPlayListId)) {
                return@launch
            }
            val response = ApiClient.getVideos(mPlayListId!!,1)
            if (response == null) {
                Log.d(TAG, "loadVideoData() called")
                return@launch
            }
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
        GlobalScope.launch {
            if (TextUtils.isEmpty(mPlayListId)) {
                return@launch
            }
            val response = ApiClient.getVideos(mPlayListId!!, mNextPageIndex)
            if (response == null) {
                Log.d(TAG, "loadVideoData() called")
                return@launch
            }
            //记录下一屏幕的数据
            mNextPageIndex = response.nextPageIndex
            isLoadDataMoreState = true
            val videos = response.items
            withContext(Dispatchers.Main) {
                mVideoDataList.value = videos
            }
        }
    }

    companion object {
        private const val TAG = "VideoListViewModel"
    }
}