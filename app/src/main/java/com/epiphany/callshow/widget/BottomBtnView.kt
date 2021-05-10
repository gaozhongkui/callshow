package com.epiphany.callshow.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.children
import com.epiphany.call.extensions.safeTypeArray
import com.epiphany.callshow.R

class BottomBtnView(context: Context?, attrs: AttributeSet?) : ShimmerLayout(context!!, attrs) {
    private val root: ViewGroup
    private var mWidthRadioByParent = 7f / 9f

    init {
        LayoutInflater.from(context).inflate(R.layout.view_bottom_btn, this, true)
        root = findViewById(R.id.root)
        val tv = findViewById<TextView>(R.id.tv_bottom)
        //引用系统资源
        val arrays = intArrayOf(android.R.attr.text, R.styleable.BottomBtnView_b_btn_width_radio)
        context!!.safeTypeArray(attrs, R.styleable.BottomBtnView) {
            val text = it.getString(R.styleable.BottomBtnView_android_text)
            tv.text = text
            mWidthRadioByParent = it.getFloat(R.styleable.BottomBtnView_b_btn_width_radio, 7f / 9f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val realWidthSpec = MeasureSpec.makeMeasureSpec((widthSize * mWidthRadioByParent).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(realWidthSpec, heightMeasureSpec)
    }

    override fun setEnabled(enabled: Boolean) {
        for (child in root.children) {
            child.isEnabled = enabled
        }
        if (enabled) {
            startShimmerAnimation()
        } else {
            stopShimmerAnimation()
        }
        super.setEnabled(enabled)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        for (child in root.children) {
            child.isSelected = selected
        }
    }

    fun setTextRes(@StringRes id: Int) {
        findViewById<TextView>(R.id.tv_bottom).setText(id)
    }
}