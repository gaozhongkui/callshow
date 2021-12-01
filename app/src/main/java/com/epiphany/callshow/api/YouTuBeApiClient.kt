package com.epiphany.callshow.api

import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import com.epiphany.callshow.App
import com.epiphany.callshow.BuildConfig
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.model.VideoItemInfo
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.youtube.YouTube
import java.io.IOException
import java.lang.Exception
import kotlin.random.Random

/**
 * 此类用于承载所有的网络请求的入口
 */
object YouTuBeApiClient {
    private const val TAG = "ApiClient"

    //分页最大返回数
    private const val NUMBER_OF_VIDEOS_RETURNED: Long = 50

    /**
     * 获取指定的视频列表
     */
    fun getVideos(playListId: String, nextPageToken: String? = null): VideoResponse? {
        val youtube = YouTube.Builder(
            YouTuBeAuth.HTTP_TRANSPORT, YouTuBeAuth.JSON_FACTORY
        ) {}.setApplicationName(BuildConfig.APPLICATION_ID).build()

        val search = try {
            youtube.playlistItems().list(mutableListOf("id,snippet"))
        } catch (e: IOException) {
            null
        }
        search?.let {
            search.key = YouTuBeAPIKeyHelper.getAPIKey()
            search.maxResults = NUMBER_OF_VIDEOS_RETURNED
            search.fields =
                "items(snippet/title,snippet/thumbnails,snippet/resourceId/videoId),nextPageToken,pageInfo,prevPageToken"
            search.playlistId = playListId
            search.pageToken = nextPageToken
            val searchResponse = try {
                search.execute()
            } catch (e: Exception) {
                //判断是否为账号错误的问题
                if (e is GoogleJsonResponseException) {
                    //youtube api 配额用尽，尝试替换api key
                    if (403 == e.details.code) {
                        //切换下一个有用的API Key
                        YouTuBeAPIKeyHelper.switchNextEnableAPIKey()
                        //重新调用
                        return getVideos(playListId, nextPageToken)
                    }
                } else {
                    if (!SystemInfo.checkConnectivity(App.getApp())) {
                        return null
                    }
                    //延迟一下，继续请求
                    SystemClock.sleep(1000)
                    return getVideos(playListId, nextPageToken)
                }

                null
            }
            Log.d(TAG, "getVideos() called $searchResponse")

            searchResponse?.apply {
                //转换数据格式
                val videos = YouTuBeVideoHelper.convertPlaylistItemToVideoInfo(items)
                val videoResponse = VideoResponse(videos, nextPageToken)
                //填充视频详情
                fillVideoDetails(videos)
                return videoResponse
            }


        }
        return null
    }


    /**
     * 填充视频详情
     */
    private fun fillVideoDetails(list: List<VideoItemInfo>) {
        val youtube = YouTube.Builder(
            YouTuBeAuth.HTTP_TRANSPORT, YouTuBeAuth.JSON_FACTORY
        ) {}.setApplicationName(BuildConfig.APPLICATION_ID).build()
        val search = try {
            youtube.videos().list(mutableListOf("id,statistics"))
        } catch (e: IOException) {
            null
        }
        search?.let {
            search.fields = "items(id,statistics)"
            search.key = YouTuBeAPIKeyHelper.getAPIKey()
            search.maxResults = NUMBER_OF_VIDEOS_RETURNED
            search.id = getVideoIdsByVideoItemInfo(list)
            val searchResponse = try {
                search.execute()
            } catch (e: Exception) {
                null
            }
            searchResponse?.apply {
                for (item in items) {
                    for (itemInfo in list) {
                        //判断两个Video Id相同时，则进行处理
                        if (TextUtils.equals(itemInfo.videoId, item.id)) {
                            item.statistics?.apply {
                                itemInfo.viewCount =
                                    if (viewCount != null) viewCount.toLong() else getRandomCount()
                                itemInfo.likeCount =
                                    if (likeCount != null) likeCount.toLong() else getRandomCount()
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取视频的ID
     */
    private fun getVideoIdsByVideoItemInfo(list: List<VideoItemInfo>): List<String> {
        val videoIds = mutableListOf<String>()
        for (itemInfo in list) {
            videoIds.add(itemInfo.videoId)
        }
        return videoIds
    }

    /**
     * 获取随机数
     */
    private fun getRandomCount(): Long {
        return 6000 + Random.nextLong(6000)
    }


}