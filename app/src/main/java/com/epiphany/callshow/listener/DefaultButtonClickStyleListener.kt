package com.epiphany.callshow.listener

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

/**
 * Author:gaozhongkui
 * Description:按钮默认点击效果的监听
 */
class DefaultButtonClickStyleListener : OnTouchListener {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == null) {
            return false
        }
        event?.apply {
            val action = action
            if (action == MotionEvent.ACTION_DOWN) {
                v.alpha = DEFAULT_DOWN_ALPHA
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                v.alpha = DEFAULT_NORMAL_ALPHA
            }
        }
        return false
    }

    companion object {
        private const val DEFAULT_DOWN_ALPHA = 0.65f
        private const val DEFAULT_NORMAL_ALPHA = 1f
    }
}