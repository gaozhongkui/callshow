package com.epiphany.callshow.function.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.opengl.GLES20
import android.opengl.GLES30
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

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


    /**
     * createVideoThumbnailFromUri
     * @param context Activity context or application context.
     * @param uri Video uri.
     * @return Bitmap thumbnail
     *
     * Hacked from ThumbnailUtils.createVideoThumbnail()'s code.
     */
    fun createVideoThumbnailFromUri(context: Context, uri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            bitmap = retriever.getFrameAtTime(-1)
        } catch (e: IllegalArgumentException) {
            // Assume this is a corrupt video file
            e.printStackTrace()
        } catch (e: RuntimeException) {
            // Assume this is a corrupt video file.
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (e: RuntimeException) {
                // Ignore failures while cleaning up.
                e.printStackTrace()
            }
        }
        if (bitmap == null) {
            return null
        }
        // Scale down the bitmap if it's too large.
        val width = bitmap.width
        val height = bitmap.height
        val max = Math.max(width, height)
        if (max > 512) {
            val scale = 512f / max
            val w = Math.round(scale * width)
            val h = Math.round(scale * height)
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true)
        }
        return bitmap
    }

    @JvmStatic
    @Throws(RuntimeException::class)
    fun compileShaderResourceGLES30(context: Context, shaderType: Int, shaderRes: Int): Int {
        val inputStream = context.resources.openRawResource(shaderRes)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        val stringBuilder = StringBuilder()
        try {
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                stringBuilder.append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return 0
        }
        val shaderSource = stringBuilder.toString()
        val shader = GLES30.glCreateShader(shaderType)
        if (shader == 0) {
            throw RuntimeException("Failed to create shader")
        }
        GLES30.glShaderSource(shader, shaderSource)
        GLES30.glCompileShader(shader)
        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException(log)
        }
        return shader
    }

    @JvmStatic
    @Throws(RuntimeException::class)
    fun linkProgramGLES30(vertShader: Int, fragShader: Int): Int {
        val program = GLES30.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Failed to create program")
        }
        GLES30.glAttachShader(program, vertShader)
        GLES30.glAttachShader(program, fragShader)
        GLES30.glLinkProgram(program)
        val status = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(program)
            GLES30.glDeleteProgram(program)
            throw RuntimeException(log)
        }
        return program
    }

    @JvmStatic
    @Throws(RuntimeException::class)
    fun compileShaderResourceGLES20(context: Context, shaderType: Int, shaderRes: Int): Int {
        val inputStream = context.resources.openRawResource(shaderRes)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        val stringBuilder = StringBuilder()
        try {
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                stringBuilder.append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return 0
        }
        val shaderSource = stringBuilder.toString()
        val shader = GLES20.glCreateShader(shaderType)
        if (shader == 0) {
            throw RuntimeException("Failed to create shader")
        }
        GLES20.glShaderSource(shader, shaderSource)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException(log)
        }
        return shader
    }

    @JvmStatic
    @Throws(RuntimeException::class)
    fun linkProgramGLES20(vertShader: Int, fragShader: Int): Int {
        val program = GLES20.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Failed to create program")
        }
        GLES20.glAttachShader(program, vertShader)
        GLES20.glAttachShader(program, fragShader)
        GLES20.glLinkProgram(program)
        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            throw RuntimeException(log)
        }
        return program
    }
}