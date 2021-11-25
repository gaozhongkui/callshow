package com.epiphany.callshow.function.web

import android.content.Context
import android.content.Intent
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
        binding.webView.settings.javaScriptEnabled = true
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