package com.epiphany.callshow.function.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.epiphany.callshow.App
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.model.HomeTabInfo

class HomeViewModel : BaseViewModel() {

    //Tab数据集合
    private val mTabDataList = MutableLiveData<List<HomeTabInfo>>()

    fun getTabDataList(): LiveData<List<HomeTabInfo>> {
        return mTabDataList
    }

    /**
     * 加载数据
     */
    fun loadTabData() {
        val resources = App.getApp().resources
        val dataList = mutableListOf<HomeTabInfo>()

        //推荐
        val recommend = HomeTabInfo(
            "beautiful",
            resources.getString(R.string.tab_recommend)
        )
        dataList.add(recommend)

        //炫酷
        val cool = HomeTabInfo(
            "Cool",
            resources.getString(R.string.tab_cool)
        )
        dataList.add(cool)

        //美女
        val beauty = HomeTabInfo(
            "beauty",
            resources.getString(R.string.tab_beauty)
        )
        dataList.add(beauty)

        //帅哥
        val handsome_guy = HomeTabInfo(
            "Phandsome guy",
            resources.getString(R.string.tab_handsome_guy)
        )
        dataList.add(handsome_guy)

        //卡通
        val cartoons = HomeTabInfo(
            "Cartoon",
            resources.getString(R.string.tab_cartoons)
        )
        dataList.add(cartoons)

        //野外
        val landscape = HomeTabInfo(
            "open country",
            resources.getString(R.string.tab_landscape)
        )
        dataList.add(landscape)

        //学生
        val funny = HomeTabInfo(
            "student",
            resources.getString(R.string.tab_funny)
        )
        dataList.add(funny)

        mTabDataList.value = dataList
    }
}