package com.epiphany.callshow

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.webkit.WebView
import com.chaquo.python.android.PyApplication

class App : Application() {
    init {
        mApplication = this
    }

    override fun onCreate() {
        super.onCreate()
        patchWebView()
    }

    fun getProcessName(context: Context?): String? {
        return if (Build.VERSION.SDK_INT >= 28) {
            getProcessName()
        } else {
            getProcessNameLegacy(context)
        }
    }

    private fun getProcessNameLegacy(context: Context?): String? {
        if (context == null) return null
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val infoList = manager.runningAppProcesses
        if (infoList != null && infoList.size > 0) {
            for (processInfo in infoList) {
                if (processInfo.pid == Process.myPid()) {
                    return processInfo.processName
                }
            }
        }
        return null
    }

    private fun patchWebView() {
        try {
            //适配安卓P，如果WebView使用多进程，添加如下代码
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName: String = getProcessName(this)!!
                val packageName = baseContext.packageName
                // 填入应用自己的包名
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName)
                }
            }
        } catch (e: Throwable) {

        }
    }

    companion object {
        private lateinit var mApplication: App

        fun getApp(): Application {
            return mApplication
        }
    }
}