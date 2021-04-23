package com.epiphany.callshow.common.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat


object StatusBarUtil {

    /**
     * 设置状态栏为透明
     * @param activity
     */
    fun setTranslucentStatus(activity: Activity, isTextDark: Boolean = false) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        val decorView = activity.window.decorView
        if (isTextDark) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }

    /**
     * @param activity
     * @param colorId
     */
    fun setStatusBarColor(activity: Activity, colorId: Int, isTextDark: Boolean = false) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, colorId)
        val decorView = activity.window.decorView
        if (isTextDark) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }

    /**
     * 设置状态栏模式
     * @param activity
     * @param isTextDark 文字、图标是否为黑色 （false为默认的白色）
     * @return
     */
    fun setStatusBarTextMode(activity: Activity, isTextDark: Boolean) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = activity.window.decorView
            if (isTextDark) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            } else {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            }
        }
    }
}
