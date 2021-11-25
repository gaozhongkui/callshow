package com.epiphany.callshow.function.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.util.Log
import android.webkit.*
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.ActivityFullWebLayoutBinding

class FullWebActivity : BaseActivity<BaseViewModel, ActivityFullWebLayoutBinding>() {
    override fun getBindLayout(): Int = R.layout.activity_full_web_layout

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        val url = intent.getStringExtra(EXTRA_URL)

        url?.let {
            binding.webView.loadUrl(url)
        }
        initWebSetting()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebSetting() {
        val mSettings: WebSettings = binding.webView.settings ?: return
        // 网页内容的宽度是否可大于WebView控件的宽度
        mSettings.loadWithOverviewMode = false
        // 保存表单数据
        mSettings.saveFormData = true
        // 是否应该支持使用其屏幕缩放控件和手势缩放
        mSettings.setSupportZoom(true)
        mSettings.builtInZoomControls = true
        mSettings.displayZoomControls = false
        // 启动应用缓存
        mSettings.setAppCacheEnabled(true)
        // 设置缓存模式
        mSettings.cacheMode = WebSettings.LOAD_DEFAULT
        // setDefaultZoom  api19被弃用
        // 设置此属性，可任意比例缩放。
        mSettings.useWideViewPort = true
        // 告诉WebView启用JavaScript执行。默认的是false。
        mSettings.javaScriptEnabled = true
        //  页面加载好以后，再放开图片
        //mSettings.setBlockNetworkImage(false)
        // 使用localStorage则必须打开
        mSettings.domStorageEnabled = true
        // 排版适应屏幕
        mSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS

        mSettings.databaseEnabled = true
        mSettings.allowFileAccess = true
        mSettings.javaScriptCanOpenWindowsAutomatically = true
        // WebView是否支持多个窗口。
        mSettings.setSupportMultipleWindows(true);
        // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
        mSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        // 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)
        //mSettings.setTextZoom(100)

        // 缩放比例 1
        binding.webView.setInitialScale(1)
        binding.webView.webViewClient = NormalWebViewClient()
        binding.webView.webChromeClient = NormalWebChromeClient()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.removeAllViews()
        binding.webView.destroy()
    }

    private inner class NormalWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.d(
                TAG,
                "onPageStarted() called with: view = $view, url = $url, favicon = $favicon"
            )
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)
            Log.d(
                TAG,
                "onReceivedSslError() called with: view = $view, handler = $handler, error = $error"
            )
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            Log.d(TAG, "onPageFinished() called with: view = $view, url = $url")
        }
    }

    private inner class NormalWebChromeClient : WebChromeClient() {

    }

    companion object {
        private const val TAG = "FullWebActivity"
        private const val EXTRA_URL = "url"
        fun launchAct(activity: Context, url: String?) {
            val intent = Intent(activity, FullWebActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }
    }
}