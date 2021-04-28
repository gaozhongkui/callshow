package com.epiphany.callshow.function.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils

object WallpaperHelper {
    const val REQUEST_SET_LIVE_WALLPAPER = 1068
    private val mHandler = Handler(Looper.getMainLooper())

    /**
     * 设置静音
     */
    fun setVoiceSilence(context: Context) {
        val intent = Intent(VIDEO_PARAMS_CONTROL_ACTION)
        intent.putExtra(ACTION, ACTION_VOICE_SILENCE)
        context.sendBroadcast(intent)
    }

    /**
     * 设置有声音
     */
    fun setVoiceNormal(context: Context) {
        val intent = Intent(VIDEO_PARAMS_CONTROL_ACTION)
        intent.putExtra(ACTION, ACTION_VOICE_NORMAL)
        context.sendBroadcast(intent)
    }

    /**
     * 获取设置壁纸的Intent
     */
    fun getWallpaperIntent(context: Context): Intent {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, VideoWallpaperService::class.java)
        )
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        return intent
    }

    /**
     * 设置壁纸
     */
    fun setToWallPaper(act: Activity, videoPath: String, audioPath: String) {
        //判断是否已经在运行中了
        val wallpaperIntent = if (isLiveWallpaperRunning(act)) {
            WallpaperPreviewActivity.getWallpaperPreviewIntent(act, videoPath, audioPath)
        } else {
            getWallpaperIntent(act)
        }
        act.startActivityForResult(wallpaperIntent, REQUEST_SET_LIVE_WALLPAPER)

        mHandler.postDelayed({ startVideoWallPaperService(act, videoPath, audioPath) }, 500)
    }

    /**
     * 启动壁纸服务
     */
    private fun startVideoWallPaperService(act: Activity, videoPath: String, audioPath: String) {
        act.startService(VideoWallpaperService.getWallpaperService(act, videoPath, audioPath))
    }

    /**
     * 判断一个动态壁纸是否已经在运行
     */
    fun isLiveWallpaperRunning(context: Context): Boolean {
        val wallpaperManager = WallpaperManager.getInstance(context) // 得到壁纸管理器
        val wallpaperInfo = wallpaperManager.wallpaperInfo // 如果系统使用的壁纸是动态壁纸话则返回该动态壁纸的信息,否则会返回null
        if (wallpaperInfo != null) { // 如果是动态壁纸,则得到该动态壁纸的包名,并与想知道的动态壁纸包名做比较
            val currentWallpaperPackageName = wallpaperInfo.packageName
            return TextUtils.equals(context.packageName, currentWallpaperPackageName)
        }
        return false
    }
}