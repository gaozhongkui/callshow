package com.epiphany.callshow.function.search

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.KeyEvent
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.ActivitySearchVideoLayoutBinding
import com.epiphany.callshow.function.home.VideoListFragment

class SearchVideoActivity : BaseActivity<SearchVideoViewModel, ActivitySearchVideoLayoutBinding>() {
    override fun getBindLayout(): Int {
        return R.layout.activity_search_video_layout
    }

    override fun getViewModelClass(): Class<SearchVideoViewModel> {
        return SearchVideoViewModel::class.java
    }

    override fun initView() {
        initStatusBarLayout()
        binding.ivBackBut.setOnClickListener {
            onBackPressed()
        }
        binding.root.post {
            binding.edText.requestFocus()
            binding.edText.isFocusableInTouchMode = true
        }
    }

    private fun initStatusBarLayout() {
        StatusBarUtil.setTranslucentStatus(this, true)
        SystemInfo.fixStatusBar(binding.toolBar, true)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        //监听搜索按键
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            showSearchContentFragment()
        }
        return super.onKeyUp(keyCode, event)
    }

    /**
     * 展示搜索内容的页面
     */
    private fun showSearchContentFragment() {
        val content = binding.edText.text.toString()
        if (TextUtils.isEmpty(content)) {
            return
        }

        //展示搜索内容
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content_view, VideoListFragment.newInstance(content))
        transaction.commitAllowingStateLoss()
    }


    companion object {

        fun launch(cxt: Context) {
            val intent = Intent(cxt, SearchVideoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            cxt.startActivity(intent)
        }
    }
}