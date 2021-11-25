package com.epiphany.callshow.function.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebSettings

open class BaseWebView(context: Context, attrs: AttributeSet?) : WebView(context, attrs) {
    private var mSettings: WebSettings? = null

    init {
        init()
    }
    protected open fun init() {
        webSetting()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webSetting() {
        mSettings = settings
        mSettings?.let {
            // 网页内容的宽度是否可大于WebView控件的宽度
            it.loadWithOverviewMode = false
            // 保存表单数据
            it.saveFormData = true
            // 是否应该支持使用其屏幕缩放控件和手势缩放
            it.setSupportZoom(true)
            it.builtInZoomControls = true
            it.displayZoomControls = false
            // 启动应用缓存
            it.setAppCacheEnabled(true)
            // 设置缓存模式
            it.cacheMode = WebSettings.LOAD_DEFAULT
            // setDefaultZoom  api19被弃用
            // 设置此属性，可任意比例缩放。
            it.useWideViewPort = true
            // 缩放比例 1
            setInitialScale(1)
            // 告诉WebView启用JavaScript执行。默认的是false。
            it.setJavaScriptEnabled(true)
            //  页面加载好以后，再放开图片
            //it.setBlockNetworkImage(false);
            // 使用localStorage则必须打开
            it.domStorageEnabled = true
            // 排版适应屏幕
            it.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
            // WebView是否支持多个窗口。
            it.setSupportMultipleWindows(true)
            // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
            it.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)
            // 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)
            //it.setTextZoom(100);
            it.databaseEnabled = true
            it.allowFileAccess = true
            it.javaScriptCanOpenWindowsAutomatically = true
        }
        webSettingsImp(mSettings)
    }

    /**
     * 由子类更加业务需求，增加 webSettings 属性
     * @param webSettings
     */
    protected open fun webSettingsImp(webSettings: WebSettings?) {}

    open fun onDestory() {
        mSettings = null
    }

}