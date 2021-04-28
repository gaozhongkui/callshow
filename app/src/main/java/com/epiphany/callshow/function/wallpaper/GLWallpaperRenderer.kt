package com.epiphany.callshow.function.wallpaper

import android.content.Context
import android.opengl.GLSurfaceView
import com.google.android.exoplayer2.SimpleExoPlayer


abstract class GLWallpaperRenderer(private val context: Context) : GLSurfaceView.Renderer {

    protected fun getContext(): Context {
        return context
    }

    abstract fun setSourcePlayer(exoPlayer: SimpleExoPlayer)
    abstract fun setScreenSize(width: Int, height: Int)
    abstract fun setVideoSizeAndRotation(width: Int, height: Int, rotation: Int)
    abstract fun setOffset(xOffset: Float, yOffset: Float)

}