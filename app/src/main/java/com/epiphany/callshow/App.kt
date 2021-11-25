package com.epiphany.callshow

import android.app.Application
import com.chaquo.python.android.PyApplication

class App : Application() {
    init {
        mApplication = this
    }

    companion object {
        private lateinit var mApplication: App

        fun getApp(): Application {
            return mApplication
        }
    }
}