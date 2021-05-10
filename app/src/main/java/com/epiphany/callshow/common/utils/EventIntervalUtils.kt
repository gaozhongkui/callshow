package com.epiphany.callshow.common.utils

import kotlin.math.abs

/**
 * Time:19-12-19-下午4:37
 * Author:gaozhongkui
 * Description:用于事件的处理工具类
 */

object EventIntervalUtils {
    private const val MIN_CLICK_INTERVAL = 500L // Min interval between two click events, ms
    private var sLastClickTime: Long = 0L

    /**
     * Function to check click events
     *
     * @return Whether the click event can be executed
     */
    @JvmStatic
    fun canClick(): Boolean {
        return canClick(MIN_CLICK_INTERVAL)
    }

    @JvmStatic
    fun canClick(clickInterval: Long): Boolean {
        val now = System.currentTimeMillis()
        if (abs(now - sLastClickTime) > MIN_CLICK_INTERVAL) {
            // Can click
            sLastClickTime = now
            return true
        }
        return false
    }
}