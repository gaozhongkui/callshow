package com.epiphany.callshow.common.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.epiphany.callshow.model.VideoItemInfo
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * 用于下载辅助类
 */
object DownloadHelper {
    private const val DEFAULT_DOWNLOAD_FILE_COUNT = 2
    private const val VIDEO_DIR = "video_dir"
    private const val TAG = "DownloadHelper"
    private val mDownloadFileCount = AtomicInteger()

    /**
     * 下载视频文件
     */
    fun downloadVideo(
        cxt: Context,
        info: VideoItemInfo,
        listener: IDownloadMangerListener? = null,
    ) {
        val videoUrl = info.videoUrl
        val audioUrl = info.audioUrl
        if (TextUtils.isEmpty(videoUrl) || TextUtils.isEmpty(audioUrl)) {
            listener?.onDownloadFailed("file name is empty")
            return
        }
        val videoFilePath = File(getDownloadDir(cxt), Hash.md5(getHttpFileName(videoUrl)))
        val audioFilePath = File(getDownloadDir(cxt), Hash.md5(getHttpFileName(audioUrl)))
        //判断如果文件已经存在时，则直接返回结果
        if (videoFilePath.exists() && audioFilePath.exists()) {
            listener?.onDownloadComplete(videoFilePath.absolutePath, audioFilePath.absolutePath)
            return
        }
        //设置默认的文件数量
        mDownloadFileCount.set(DEFAULT_DOWNLOAD_FILE_COUNT)
        val downloadManager = cxt.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val videoRequest = DownloadManager.Request(Uri.parse(videoUrl))
        val audioRequest = DownloadManager.Request(Uri.parse(audioUrl))
        videoRequest.setDestinationUri(Uri.fromFile(videoFilePath))
        audioRequest.setDestinationUri(Uri.fromFile(audioFilePath))
        videoRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        audioRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        downloadManager.enqueue(videoRequest)
        downloadManager.enqueue(audioRequest)
        DownloadReceiver.registerReceiver(
            cxt,
            videoFilePath.absolutePath,
            audioFilePath.absolutePath,
            listener
        )
    }

    /**
     * 获取下载目录
     */
    private fun getDownloadDir(cxt: Context): String? {
        return cxt.getExternalFilesDir(VIDEO_DIR)?.absolutePath
//        return "${Environment.getExternalStorageDirectory()}/$VIDEO_DIR"

    }

    /**
     * 获取文件名称
     */
    private fun getHttpFileName(filePath: String?): String {
        if (TextUtils.isEmpty(filePath)) {
            return System.currentTimeMillis().toString()
        }
        val index = filePath!!.indexOf("?")
        if (index <= 0) {
            return filePath
        }
        return filePath.substring(index + 1)
    }

    private class DownloadReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.apply {
                if (action == null) {
                    return
                }
                //判断是否为下载完成
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    Log.d(TAG, "onReceive() called")

                    val remainingFileCount = mDownloadFileCount.decrementAndGet()
                    //剩余文件数量,判断如果大于0时,则直接返回
                    if (remainingFileCount > 0) {
                        return
                    }

                    listener?.apply {
                        if (TextUtils.isEmpty(videoDestFilePath)
                            || TextUtils.isEmpty(audioDestFilePath)
                        ) return
                        onDownloadComplete(videoDestFilePath!!, audioDestFilePath!!)
                    }
                    unRegisterReceiver(context!!)
                }
            }
        }


        companion object {
            private var receiver: DownloadReceiver? = null
            private var listener: IDownloadMangerListener? = null
            private var videoDestFilePath: String? = null
            private var audioDestFilePath: String? = null
            fun registerReceiver(
                context: Context,
                videoDestFilePath: String,
                audioDestFilePath: String,
                listener: IDownloadMangerListener? = null
            ) {
                receiver = DownloadReceiver()
                this.listener = listener
                this.videoDestFilePath = videoDestFilePath
                this.audioDestFilePath = audioDestFilePath
                val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                try {
                    context.registerReceiver(receiver, intentFilter)
                } catch (e: Exception) {
                }
            }

            fun unRegisterReceiver(context: Context) {
                if (receiver == null) {
                    return
                }
                try {
                    context.unregisterReceiver(receiver)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                receiver = null
            }
        }

    }


    interface IDownloadMangerListener {
        fun onDownloadComplete(videoUrl: String, audioUrl: String)
        fun onDownloadFailed(reason: String)
    }
}