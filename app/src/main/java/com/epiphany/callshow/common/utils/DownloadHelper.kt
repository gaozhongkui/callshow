package com.epiphany.callshow.common.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.epiphany.callshow.model.VideoItemInfo
import java.io.File

/**
 * 用于下载辅助类
 */
object DownloadHelper {
    private const val VIDEO_DIR = "video_dir"
    private const val TAG = "DownloadHelper"

    /**
     * 下载视频文件
     */
    fun downloadVideo(
        cxt: Context,
        info: VideoItemInfo,
        listener: IDownloadMangerListener? = null,
        //给一个默认名字
        fileName: String = getFileName(info.videoUrl)
    ) {
        val videoUrl = info.videoUrl
        if (TextUtils.isEmpty(videoUrl)) {
            return
        }
        val destFilePath = File(getDownloadDir(cxt), fileName)
        //判断如果文件已经存在时，则直接返回结果
        if (destFilePath.exists()) {
            listener?.onDownloadComplete(destFilePath.absolutePath)
            return
        }

        val downloadManager = cxt.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(videoUrl))
        request.setDestinationUri(Uri.fromFile(destFilePath))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        val enqueue = downloadManager.enqueue(request)
        DownloadReceiver.registerReceiver(cxt, destFilePath.absolutePath, listener)
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
    private fun getFileName(filePath: String?): String {
        if (TextUtils.isEmpty(filePath)) {
            return System.currentTimeMillis().toString()
        }
        val file = File(filePath)
        val index = file.name.indexOf("?")
        if (index <= 0) {
            return filePath!!
        }
        return file.name.substring(0, index)
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

                    listener?.apply {
                        if (destFilePath != null) {
                            onDownloadComplete(destFilePath!!)
                        }
                    }
                    unRegisterReceiver(context!!)
                }
            }
        }


        companion object {
            private var receiver: DownloadReceiver? = null
            private var listener: IDownloadMangerListener? = null
            private var destFilePath: String? = null
            fun registerReceiver(
                context: Context,
                filePath: String,
                listener: IDownloadMangerListener? = null
            ) {
                receiver = DownloadReceiver()
                this.listener = listener
                this.destFilePath = filePath
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
        fun onDownloadComplete(filePath: String)
        fun onDownloadFailed(reason: String)
    }
}