package com.epiphany.callshow.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.epiphany.callshow.common.utils.SystemInfo

//状态栏高度
class StateBarView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = SystemInfo.getStatusBarHeight(context)
        setMeasuredDimension(width, height)
    }
}