package com.epiphany.callshow

import android.app.Application
import com.chaquo.python.android.PyApplication

class App : PyApplication() {
    init {
        mApplication = this
    }

    override fun onCreate() {
        super.onCreate()

    }

    companion object {
        private lateinit var mApplication: App

        fun getApp(): Application {
            return mApplication
        }
    }
}