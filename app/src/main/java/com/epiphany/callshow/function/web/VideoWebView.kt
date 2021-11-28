package com.epiphany.callshow.function.web

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.view.View
import android.webkit.JavascriptInterface
import java.net.URISyntaxException
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.os.Build
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient.CustomViewCallback
import android.webkit.WebViewClient
import android.webkit.WebSettings
import com.epiphany.callshow.function.web.TagUtils.getJs


class VideoWebView(context: Context, attrs: AttributeSet?) : BaseWebView(context, attrs) {
    private var mOnVideoWebViewListener: OnVideoWebViewListener? = null
    private var mCallback: CustomViewCallback? = null
    private var mFullScreenMode = false //是否进入了横屏全屏模式

    private var mIgnoreSslError = false //是否忽略ssl证书错误

    //页面加载的事件监听
    private var mIPageLoadListener: IPageLoadListener? = null
    override fun initLayout() {
        super.initLayout()
        this.webViewClient = CustomWebClient()
        this.webChromeClient = CustomWebChromClient()
        addJavascriptInterface(VideoJsObject(), "onClickFullScreenBtn")
    }

    override fun webSettingsImp(webSettings: WebSettings?) {
        super.webSettingsImp(webSettings)
    }

    fun setIPageLoadListener(pageLoadListener: IPageLoadListener) {
        mIPageLoadListener = pageLoadListener
    }

    /**
     * 模拟点击
     */
    private fun simulateTouchEvent(view: View, x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_DOWN, x, y, metaState
        )
        view.dispatchTouchEvent(motionEvent)
        val motionMoveEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_MOVE, x, y, metaState
        )
        view.dispatchTouchEvent(motionMoveEvent)
        val upEvent = MotionEvent.obtain(
            downTime + 1000, eventTime + 1000,
            MotionEvent.ACTION_UP, x, y, metaState
        )
        view.dispatchTouchEvent(upEvent)
    }

    /**
     * 响应改变浏览器中装饰元素的事件（JavaScript 警告，网页图标，状态条加载，网页标题的刷新，进入／退出全屏）
     */
    private inner class CustomWebChromClient : WebChromeClient() {
        /**
         * 常规方式下，点击全屏按钮的时候调
         * @param view
         * @param callback
         */
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            super.onShowCustomView(view, callback)
            mCallback = callback
            //回调给外层，由外层处理全屏触发后的逻辑
            if (null != mOnVideoWebViewListener) {
                mOnVideoWebViewListener?.onShowCustomView(view, callback)
            }
            mFullScreenMode = true //标识全屏
        }

        /**
         * 常规方式下，点击退出全屏的时候调
         */
        override fun onHideCustomView() {
            super.onHideCustomView()
            //回调给外层，由外层处理退出全屏的逻辑
            if (null != mOnVideoWebViewListener) {
                mOnVideoWebViewListener?.onHideCustomView(mCallback)
            }
            mFullScreenMode = false //标识退出全屏
        }
    }

    private inner class CustomWebClient : WebViewClient() {
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            if (mIgnoreSslError) {
                // let's ignore ssl error
                handler.proceed()
            } else {
                super.onReceivedSslError(view, handler, error)
            }
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            mIPageLoadListener?.onPageStarted(url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            mIPageLoadListener?.onPageFinished(url)
            //页面加载完成的时候，注入js
            val js = getJs(url)
            view.loadUrl(js)
            view.loadUrl("javascript:onClickFullScreenBtn.fullButLocation(document.getElementsByClassName('mgp_btn mgp_maximize mgp_icon mgp_icon-fullscreen')[0].getBoundingClientRect().x/window.innerWidth,document.getElementsByClassName('mgp_btn mgp_maximize mgp_icon mgp_icon-fullscreen')[0].getBoundingClientRect().y/window.innerHeight)")
        }

        /**
         * (此接口 Android N 以后 deprecation)
         * true: 不处理这个url，我自己来； false：webView加载这个url，我什么都不做
         */
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val uri = Uri.parse(url)
            return handleUri(view, uri)
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri = request.url
            return handleUri(view, uri)
        }

        private fun handleUri(view: WebView, uri: Uri): Boolean {
            val url = uri.toString()
            //final String host = uri.getHost();  //m.youku.com
            //final String scheme = uri.getScheme();  // http
            /**
             * 此处用来处理，部分机型加载网页，有时除了返回url。
             * 还有返回带有 intent:// 的格式，该格式带有启动app的action，可以启动对应的app
             */
            if (url.startsWith("intent://")) {
                try {
                    val context = view.context
                    val intent: Intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    if (intent != null) {
                        view.stopLoading()
                        val packageManager = context.packageManager
                        val info = packageManager.resolveActivity(
                            intent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )
                        if (info != null) {
                            context.startActivity(intent)
                        } else {
                            val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                            view.loadUrl(fallbackUrl!!)

                            // or call external broswer
                            //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
                            //context.startActivity(browserIntent);
                        }
                        return true
                    }
                } catch (e: URISyntaxException) {
                }
            }
            return false
        }
    }

    private inner class VideoJsObject {
        /**
         * 从线程回调，要更新 UI 操作，要 post 到主线程
         */
        @JavascriptInterface
        fun fullscreen() {
            if (null != mOnVideoWebViewListener) {
                if (mFullScreenMode) {
                    mOnVideoWebViewListener?.onJsExitFullScreenMode()
                } else {
                    mOnVideoWebViewListener?.onJsEnterFullSceenMode()
                }
                mFullScreenMode = !mFullScreenMode //重置全屏状态
            }
        }

        @JavascriptInterface
        fun fullButLocation(sx: Float, sy: Float) {
            Log.d("gaozhongkui", "fullButLocation() called with: sx = $sx, sy = $sy")
            post {
                simulateTouchEvent(this@VideoWebView, width * sx, height * sy)
            }
        }
    }

    /**
     * 设置进入／退出全屏的监听
     * @param onVideoWebViewListener
     */
    fun setOnVideoWebViewListener(onVideoWebViewListener: OnVideoWebViewListener?) {
        mOnVideoWebViewListener = onVideoWebViewListener
    }

    interface OnVideoWebViewListener {
        /**
         * 注入方式：进入横屏全屏模式
         */
        fun onJsEnterFullSceenMode()

        /**
         * 注入方式：退出横屏全屏模式
         */
        fun onJsExitFullScreenMode()

        /**
         * 常规方式：点击网页上的全屏按钮时，执行此方法
         * @param view
         * @param callback
         */
        fun onShowCustomView(view: View?, callback: CustomViewCallback?)

        /**
         * 常规方式：再次点击网页上的全屏按钮，执行此方法
         * @param callback
         */
        fun onHideCustomView(callback: CustomViewCallback?)
    }

    /**
     * 获取当前是否全屏的状态
     * @return
     */
    fun getFullScreenMode(): Boolean {
        return mFullScreenMode
    }

    /**
     * 设置是否为全屏模式
     * @param fullScreenMode
     */
    fun setFullScreenMode(fullScreenMode: Boolean) {
        mFullScreenMode = fullScreenMode
    }

    /**
     * 设置是否忽略ssl验证错误
     * @param ignoreSslError
     */
    fun setIgnoreSslError(ignoreSslError: Boolean) {
        mIgnoreSslError = ignoreSslError
    }

    /**
     * 外部手动销毁
     */
    override fun onDestory() {
        super.onDestory()
        mIPageLoadListener = null
        mOnVideoWebViewListener = null
        mCallback = null
        stopLoading()
        clearCache(true)
        clearFormData()
        clearMatches()
        clearHistory()
        clearDisappearingChildren()
        clearAnimation()
        removeAllViews()
        destroy()
    }
}

interface IPageLoadListener {
    fun onPageStarted(url: String?)

    fun onPageFinished(url: String?)
}