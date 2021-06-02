package com.epiphany.callshow.function.home

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.epiphany.callshow.model.HomeTabInfo

/**
 * 主页视频页面
 */
class HomePageAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val mDataList = mutableListOf<HomeTabInfo>()
    private val mCacheFragments = mutableMapOf<Int, Fragment>()

    override fun getCount(): Int = mDataList.size

    override fun getItem(position: Int): Fragment {
        var fragment = mCacheFragments[position]
        //判断为空时，则进行创建对象
        if (fragment == null) {
            fragment = VideoListFragment.newInstance(mDataList[position].playListId)
            mCacheFragments[position] = fragment
        }
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mDataList[position].tabTitle
    }

    @MainThread
    fun setDataList(dataList: List<HomeTabInfo>) {
        mDataList.clear()
        mDataList.addAll(dataList)
        notifyDataSetChanged()
    }

    fun releaseData() {
        mDataList.clear()
        mCacheFragments.clear()
    }
}

