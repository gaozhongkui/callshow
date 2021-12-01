package com.epiphany.callshow.function.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.epiphany.callshow.App
import com.epiphany.callshow.R
import com.epiphany.callshow.api.APiClientManager
import com.epiphany.callshow.api.APiClientManager.VIDEO_PLAY_MODE
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
        //判断是否为pornhub模式
        if (VIDEO_PLAY_MODE == APiClientManager.VideoType.PornHub) {
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
        } else {
            //推荐
            val recommend = HomeTabInfo(
                "PLIbLfYSA8ACNYCOaDWmj6EA1F1uS-pyVL",
                resources.getString(R.string.tab_recommend)
            )
            dataList.add(recommend)

            //炫酷
            val cool = HomeTabInfo(
                "PLj6XzcqwRpN73wfRp1w7j_PK2OLrbsR-A",
                resources.getString(R.string.tab_cool)
            )
            dataList.add(cool)

            //美女
            val beauty = HomeTabInfo(
                "PLbduwZ5ABnEmprn8d5OdIjUZwZnWsXjrn",
                resources.getString(R.string.tab_beauty)
            )
            dataList.add(beauty)

            //帅哥
            val handsome_guy = HomeTabInfo(
                "PLgjZiTE-Q9SJZRivhaB6HQLeLmYMSgZEH",
                resources.getString(R.string.tab_handsome_guy)
            )
            dataList.add(handsome_guy)

            //卡通
            val cartoons = HomeTabInfo(
                "PLgjZiTE-Q9SI71HKolVdpMdjo7F6oXAsL",
                resources.getString(R.string.tab_cartoons)
            )
            dataList.add(cartoons)

            //风景
            val landscape = HomeTabInfo(
                "PLgjZiTE-Q9SI7D44Bfp1Kv0gatv7nQZuy",
                resources.getString(R.string.tab_landscape)
            )
            dataList.add(landscape)

            //搞笑
            val funny = HomeTabInfo(
                "PLgjZiTE-Q9SLjtSl3u0ApQ1Lk6a2reDbu",
                resources.getString(R.string.tab_funny)
            )
        }

        mTabDataList.value = dataList
    }
}