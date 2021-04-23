package com.epiphany.callshow.common.utils

import android.content.Context
import android.content.SharedPreferences
import com.epiphany.callshow.App

class SharedPreferenceUtil {

    companion object {

        private val preference: SharedPreferences = App.getApp().getSharedPreferences("common", Context.MODE_PRIVATE)

        fun getLong(key: String, defValue: Long = 0L) = preference.getLong(key, defValue)

        fun setLong(key: String, value: Long) {
            preference.edit().putLong(key, value).apply()
        }

        fun getBoolean(key: String, defValue: Boolean = false) = preference.getBoolean(key, defValue)

        fun setBoolean(key: String, value: Boolean) {
            preference.edit().putBoolean(key, value).apply()
        }

        fun getInt(key: String, defValue: Int = 0) = preference.getInt(key, defValue)

        fun setInt(key: String, value: Int) {
            preference.edit().putInt(key, value).apply()
        }

        fun getString(key: String, defValue: String = "") = preference.getString(key, defValue)

        fun setString(key: String, value: String) {
            preference.edit().putString(key, value).apply()
        }

        fun getFloat(key: String, defValue: Float = 0F) = preference.getFloat(key, defValue)

        fun setFloat(key: String, value: Float) {
            preference.edit().putFloat(key, value).apply()
        }

    }

}