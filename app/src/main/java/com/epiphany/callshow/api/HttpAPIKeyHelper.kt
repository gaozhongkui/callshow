package com.epiphany.callshow.api

import android.content.Context
import android.content.SharedPreferences
import com.epiphany.callshow.App

/**
 * 请求用于YouTube API KEY的辅助类
 */
object HttpAPIKeyHelper {
    private const val SHARE_FILE_NAME = "app_api_key"
    private const val KEY_CURRENT_API_KEY = "key_current_api_key"

    //备用的API KEY 用于YouTube
    private val mSpareApiKeyList = listOf("AIzaSyCOixs9H_34-tt3cg8AxpO2NyTO0eqfX_o")

    /**
     * 获取可用的API KEY
     */
    fun getAPIKey(): String {
        val value = getSharePre().getString(KEY_CURRENT_API_KEY, mSpareApiKeyList[0])
        value?.apply {
            return this
        }
        return mSpareApiKeyList[0]
    }

    /**
     * 切换下一条有用的API Key
     */
    fun switchNextEnableAPIKey() {


    }


    private fun getSharePre(): SharedPreferences {
        val app = App.getApp()
        return app.getSharedPreferences(SHARE_FILE_NAME, Context.MODE_PRIVATE)
    }

}