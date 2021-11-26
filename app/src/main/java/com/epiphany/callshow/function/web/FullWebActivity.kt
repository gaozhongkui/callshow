package com.epiphany.callshow.function.web

import android.content.Context
import android.content.Intent
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.ActivityFullWebLayoutBinding

import android.view.View
import android.webkit.WebChromeClient.CustomViewCallback
import com.epiphany.callshow.function.web.VideoWebView.OnVideoWebViewListener
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.widget.FrameLayout

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
        binding.webView.setIgnoreSslError(true)
        binding.webView.setOnVideoWebViewListener(VideoWebViewListenerImp())
        binding.webView.setIPageLoadListener(object : IPageLoadListener {
            override fun onPageStarted(url: String?) {
                binding.loadingView.visibility = View.VISIBLE
                binding.loadingView.playAnimation()
            }

            override fun onPageFinished(url: String?) {
                binding.loadingView.visibility = View.GONE
                binding.loadingView.pauseAnimation()
            }

        })
    }

    /**
     * 设置横屏
     */
    private fun setLandscape() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    /**
     * 设置竖屏
     */
    private fun setPortrait() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    /**
     * 常规方式：设置全屏
     */
    private fun setFullScreen(view: View?) {
        binding.webView.visibility = View.GONE
        binding.framelayoutContainer.addView(
            view,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        binding.framelayoutContainer.setVisibility(View.VISIBLE)
    }

    /**
     * 常规方式：设置正常模式（非全屏）
     */
    private fun setNormalScreen() {
        binding.framelayoutContainer.removeAllViews()
        binding.framelayoutContainer.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    /**
     * 自定义全屏接口实现类
     */
    inner class VideoWebViewListenerImp : OnVideoWebViewListener {
        override fun onJsEnterFullSceenMode() {
            setLandscape()
        }

        override fun onJsExitFullScreenMode() {
            setPortrait()
        }

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            setLandscape()
            setFullScreen(view)
        }

        override fun onHideCustomView(callback: CustomViewCallback?) {
            setPortrait()
            callback?.onCustomViewHidden()
            setNormalScreen()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.onDestory()
    }

    companion object {
        private const val EXTRA_URL = "url"
        fun launchAct(activity: Context, url: String?) {
            val intent = Intent(activity, FullWebActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            activity.startActivity(intent)
        }
    }
}