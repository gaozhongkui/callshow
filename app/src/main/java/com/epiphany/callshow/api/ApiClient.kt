package com.epiphany.callshow.api

import android.util.Log
import com.epiphany.callshow.BuildConfig
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.PlaylistItemListResponse
import java.io.IOException

/**
 * 此类用于承载所有的网络请求的入口
 */
object ApiClient {
    private const val TAG = "ApiClient"

    //分页最大返回数
    private const val NUMBER_OF_VIDEOS_RETURNED: Long = 50

    /**
     * 获取指定的视频列表
     */
    fun getVideos(playListId: String, nextPageToken: String? = null): PlaylistItemListResponse? {
        val l = System.currentTimeMillis()
        val youtube = YouTube.Builder(
            Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY
        ) {}.setApplicationName(BuildConfig.APPLICATION_ID).build()

        val search = try {
            youtube.playlistItems().list(mutableListOf("id,snippet"))
        } catch (e: IOException) {
            null
        }
        search?.let {
            search.key = APIKeyHelper.getAPIKey()
            search.maxResults = NUMBER_OF_VIDEOS_RETURNED
            search.fields =
                "items(snippet/title,snippet/thumbnails,snippet/resourceId/videoId),nextPageToken,pageInfo,prevPageToken"
            search.playlistId = playListId
            search.pageToken = nextPageToken
            val searchResponse = try {
                search.execute()
            } catch (e: GoogleJsonResponseException) {
                //youtube api 配额用尽，尝试替换api key
                if (403 == e.details.code) {
                    //切换下一个有用的API Key
                    APIKeyHelper.switchNextEnableAPIKey()
                    //重新调用
                    return getVideos(playListId, nextPageToken)
                }
                null
            }
            Log.d(TAG, "getVideos() called $searchResponse")
            return searchResponse
        }
        return null
    }

}