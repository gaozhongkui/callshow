package com.epiphany.callshow.function.video

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.epiphany.callshow.api.ApiClient
import com.epiphany.callshow.api.VideoHelper.convertPlaylistItemToVideoInfo
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.function.home.HomeViewModel
import com.epiphany.callshow.model.VideoItemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoDisplayViewModel : BaseViewModel() {
    //下一页的Token
    private var mNextPageToken: String? = null

    //加载更多数据的状态
    private var isLoadDataMoreState = false

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
            val response = ApiClient.getVideos(PLAY_LIST_ID, mNextPageToken)
            if (response == null) {
                Log.d(TAG, "loadVideoData() called")
                return@launch
            }
            //记录下一屏幕的数据
            mNextPageToken = response.nextPageToken
            isLoadDataMoreState = true
            val videos = response.items
            withContext(Dispatchers.Main) {
                mVideoDataList.value = videos
            }
        }
    }


    companion object {
        private const val PLAY_LIST_ID = "PLbduwZ5ABnEmprn8d5OdIjUZwZnWsXjrn"
        private const val TAG = "HomeViewModel"
    }
}