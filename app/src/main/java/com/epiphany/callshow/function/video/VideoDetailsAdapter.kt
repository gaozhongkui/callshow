package com.epiphany.callshow.function.video

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.epiphany.callshow.model.VideoItemInfo

class VideoDetailsAdapter(fragment: FragmentActivity) :
    FragmentStateAdapter(fragment) {
    private val mDataList = mutableListOf<VideoItemInfo>()

    override fun createFragment(position: Int): Fragment {
        return VideoFragment.newInstance(mDataList[position])
    }

    override fun getItemCount(): Int = mDataList.size

    @MainThread
    fun setDataList(dataList: List<VideoItemInfo>, isRefresh: Boolean) {
        if (isRefresh) {
            mDataList.clear()
        }
        mDataList.addAll(dataList)
        notifyDataSetChanged()
    }

    /**
     * 获取视频Info数据
     */
    fun getItemData(position: Int): VideoItemInfo? {
        if (position >= itemCount) {
            return null
        }
        return mDataList[position]
    }

    fun releaseData() {
        mDataList.clear()
    }

}