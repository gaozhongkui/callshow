package com.epiphany.callshow.function.wallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class WallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return VideoWallPagerEngine()
    }

    private inner class VideoWallPagerEngine : Engine() {

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
        }

        override fun onDestroy() {
            super.onDestroy()
        }

    }


}