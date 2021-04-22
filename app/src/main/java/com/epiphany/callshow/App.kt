package com.epiphany.callshow

import android.app.Application

class App : Application() {
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