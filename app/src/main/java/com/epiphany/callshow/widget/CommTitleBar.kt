package com.epiphany.callshow.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.epiphany.call.extensions.beGone
import com.epiphany.call.extensions.safeTypeArray
import com.epiphany.callshow.R

class CommTitleBar(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {


    private var mTitleTv: TextView
    private var mLeftIv: ImageView

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.include_title_bar, this, true)

        mTitleTv = findViewById(R.id.tv_title)
        mLeftIv = findViewById(R.id.iv_left)
        val stateBarView = findViewById<StateBarView>(R.id.state_bar)

        context?.safeTypeArray(attrs, R.styleable.CommTitleBar) {
            val hideBar = it.getBoolean(R.styleable.CommTitleBar_comm_title_hide_bar, false)
            if (hideBar) stateBarView.beGone()

            val title = it.getString(R.styleable.CommTitleBar_comm_title_title_content)
            if (!title.isNullOrEmpty()) mTitleTv.text = title

            //背景处理
            it.getDrawable(R.styleable.CommTitleBar_comm_title_background)?.let { bg ->
                findViewById<View>(R.id.group).background = bg
            }

        }
    }

    fun setBackListener(listener: View.OnClickListener) {
        mLeftIv.setOnClickListener(listener)
    }
}