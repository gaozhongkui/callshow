package com.epiphany.callshow.function.callshow

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.constraintlayout.widget.ConstraintLayout
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.common.utils.SystemInfo
import com.epiphany.callshow.databinding.ActivityCallShowDisplayBinding
import com.epiphany.callshow.function.video.VideoFragment
import com.epiphany.callshow.model.VideoItemInfo

/**
 * 用于展示来电预览的页面
 */
class CallShowDisplayActivity : BaseActivity<BaseViewModel, ActivityCallShowDisplayBinding>() {
    override fun getBindLayout(): Int = R.layout.activity_call_show_display

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        initLayoutData()
        initStatusBarLayout()
        initLayoutListener()
    }

    @SuppressLint("SetTextI18n")
    private fun initLayoutData() {
        intent?.apply {
            val videosItemInfo = getParcelableExtra<VideoItemInfo>(EXTRA_VIDEO_INFO)
            videosItemInfo?.let {
                val transaction = supportFragmentManager.beginTransaction()
                val mFragment = VideoFragment.newInstance(it,true, isShowControlView = false)
                transaction.replace(R.id.fl_content, mFragment)
                transaction.commit()
            }
        }
        binding.tvPhoneNumber.text = "18866668888"
    }

    private fun initStatusBarLayout() {
        val layoutParams = binding.ivBackBut.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topMargin = SystemInfo.getStatusBarHeight(this)
        binding.ivBackBut.layoutParams = layoutParams
    }

    private fun initLayoutListener() {
        binding.ivBackBut.setOnClickListener {
            onBackPressed()
        }
    }

    companion object {
        private const val EXTRA_VIDEO_INFO = "extra_video_info"
        fun launchActivity(cxt: Context, videoInfo: VideoItemInfo? = null) {
            val intent = Intent(cxt, CallShowDisplayActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra(EXTRA_VIDEO_INFO, videoInfo)
            cxt.startActivity(intent)
        }
    }
}