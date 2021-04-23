package com.epiphany.callshow.function.video

import android.os.Bundle
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.databinding.FragmentVideoListBinding

class VideoListFragment : BaseFragment<VideoListViewModel, FragmentVideoListBinding>() {
    override fun getBindLayout(): Int = R.layout.fragment_video_list

    override fun getViewModelClass(): Class<VideoListViewModel> {
        return VideoListViewModel::class.java
    }

    override fun initView() {
    }

    companion object {
        fun newInstance(bundle: Bundle? = null): VideoListFragment {
            val fragment = VideoListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}