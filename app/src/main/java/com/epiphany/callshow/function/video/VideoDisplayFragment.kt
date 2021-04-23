package com.epiphany.callshow.function.video

import android.os.Bundle
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.databinding.FragmentVideoDisplayBinding

class VideoDisplayFragment : BaseFragment<VideoDisplayViewModel, FragmentVideoDisplayBinding>() {
    override fun getBindLayout(): Int = R.layout.fragment_video_display

    override fun getViewModelClass(): Class<VideoDisplayViewModel> {
        return VideoDisplayViewModel::class.java
    }

    override fun initView() {
    }

    companion object {
        fun newInstance(bundle: Bundle? = null): VideoDisplayFragment {
            val fragment = VideoDisplayFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}