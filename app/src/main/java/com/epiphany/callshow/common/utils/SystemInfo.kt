package com.epiphany.callshow.common.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.epiphany.callshow.App
import java.math.BigInteger
import java.security.SecureRandom
import java.util.*


object SystemInfo {

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        if (result == 0) {
            result = dip2pxInt(context, 25)
        }
        return result
    }

    fun dip2pxInt(context: Context, dipValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    /**
     * 检查网络链接的状态
     */
    fun checkConnectivity(context: Context?): Boolean {
        context?.apply {
            try {
                val manager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkinfo = manager.activeNetworkInfo
                if (networkinfo != null && networkinfo.isConnectedOrConnecting) {
                    return true
                }
            } catch (e: Exception) {
            }
        }
        return false
    }

    fun isWiFiAvailable(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkinfo = manager.activeNetworkInfo
        return networkinfo != null && networkinfo.type == ConnectivityManager.TYPE_WIFI && networkinfo.isConnectedOrConnecting
    }


    fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    fun checkPermission(permissionName: String): Boolean {
        val isGranted = App.getApp().packageManager.checkPermission(
            App.getApp()!!.packageName,
            permissionName
        )
        return isGranted == PackageManager.PERMISSION_GRANTED
    }

    fun isValidActivity(activity: Activity?): Boolean {
        if (activity == null || activity.isFinishing) {
            return false
        }
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed)
    }


    fun fixStatusBar(view: View) {
        val padding = 0
        val insetStatusBarHeight = getInsetStatusBarHeight(view.context)
        view.setPadding(padding, insetStatusBarHeight, padding, 0)
    }

    fun fixStatusBar(view: View, useOldParamPadding: Boolean) {
        val insetStatusBarHeight = getInsetStatusBarHeight(view.context)
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        if (useOldParamPadding) {
            layoutParams.topMargin += insetStatusBarHeight
        } else {
            layoutParams.topMargin = insetStatusBarHeight
        }
        view.layoutParams = layoutParams
    }

    fun fixStatusBar2(view: View) {
        fixStatusBar(view, false)
    }

    /**
     * 在InsetDecor{<item name="android:windowTranslucentStatus">true</item>}主题下,插入状态栏的高度
     * Android 仅在4.4及以上支持此模式
     *
     * @param context
     * @return
     */
    private fun getInsetStatusBarHeight(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= 19) {
            getStatusBarHeight(context)
        } else {
            0
        }
    }

    /**
     * 获取屏幕高度
     */
    fun getScreenHeight(): Int {
        return App.getApp().resources.displayMetrics.heightPixels
    }

    /**
     * 获取屏幕的宽度
     */
    fun getScreenWidth(): Int {
        return App.getApp().resources.displayMetrics.widthPixels
    }

    //android M 上动态改变状态栏文字颜色
    fun setAndroidNativeLightStatusBar(activity: Activity, dark: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor = activity.window.decorView
                if (dark) {
                    decor.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decor.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
            }
        } catch (ignore: Exception) {
        }
    }


    /**
     * 先获取android_id，若其为空，则返回sha-1的imei，再有问题就返回"UNKNOWN"
     *
     * @param context
     * @return
     */
    fun getAndroidIdNotNull(context: Context?): String? {
        var androidId: String = getAndroidId(context!!)!!
        if (TextUtils.isEmpty(androidId)) {
            androidId = UUID.randomUUID().toString()
        }
        //return "2c236d3562c600e1";
        return androidId
    }

    fun getAndroidId(context: Context): String? {
        var androidId: String? = ""
        try {
            androidId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: java.lang.Exception) {
        }
        if (TextUtils.isEmpty(androidId)) {
            try {
                androidId =
                    Settings.System.getString(
                        context.contentResolver,
                        Settings.System.ANDROID_ID
                    )
            } catch (ignore: java.lang.Exception) {
            }
        }
        if (androidId == null || androidId.length == 0) {
            try {
                val cls = Build::class.java
                val field = cls.getDeclaredField("SERIAL")
                field.isAccessible = true
                androidId = field[null] as String
            } catch (ignore: java.lang.Exception) {
            }
        }
        Log.v("systemInfo", "get android id is $androidId")
        return androidId
    }


    fun hasTelFeature(): Int {
        return try {
            val packageManager: PackageManager = App.getApp()!!.getPackageManager()
            val hadTel = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
            Log.i("SystemInfo", "hadTel:$hadTel")
            if (hadTel) 1 else 2
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            val telephonyManager =
                App.getApp()!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager != null) {
                1
            } else {
                2
            }
        }
    }

    /**
     * 判断是否包含SIM卡
     */
    fun hasSimCard(): Int {
        val telMgr =
            App.getApp()!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (telMgr != null) {
            val simState = telMgr.simState
            Log.i("SystemInfo", "simStatus:$simState")
            if (simState == TelephonyManager.SIM_STATE_ABSENT) {
                2
            } else {
                1
            }
        } else {
            Log.i("SystemInfo", "TelephonyManager is null")
            2
        }
    }

    fun getGlobalDeviceId(appContext: Context): String? {
        var number = ""
        try {
            val manager =
                appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            number = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                manager.imei
            } else {
                manager.deviceId
            }
            if (TextUtils.isEmpty(number)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    number = manager.getDeviceId(0)
                }
            }
        } catch (e: java.lang.Exception) {
        }
        return number
    }


    fun getUser100Dimen(context: Context): Int {
        var androidId: String = getAndroidIdNotNull(context)!!
        if (TextUtils.isEmpty(androidId)) {
            androidId = Settings.System.getString(
                context.contentResolver,
                App.getApp()!!.packageName + ".random_id"
            )
            if (TextUtils.isEmpty(androidId)) {
                androidId = BigInteger(130, SecureRandom()).toString(32)
                Settings.System.putString(
                    context.contentResolver,
                    App.getApp()!!.packageName + ".random_id",
                    androidId
                )
            }
        }
        val hashCode = Math.abs(androidId.hashCode())
        return hashCode % 100
    }

    fun getSecurityPatch(): String? {
        return if (Build.VERSION.SDK_INT >= 23) {
            Build.VERSION.SECURITY_PATCH
        } else {
            getSystemProperty(
                "ro.build.version.security_patch",
                "unknown"
            )
        }
    }

    fun getSystemProperty(str: String, str2: String): String? {
        return try {
            val cls = Class.forName("android.os.SystemProperties")
            cls.getMethod("get", *arrayOf<Class<*>>(String::class.java, String::class.java))
                .invoke(cls, *arrayOf<Any>(str, str2)) as String
        } catch (e: java.lang.Exception) {
            str2
        }
    }

    fun setupWindow(activity: Activity?) {
        if (activity != null && activity.window != null) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.window.statusBarColor = Color.TRANSPARENT
            }
        }
    }

    fun setupWindow(activity: Activity?, flag: Int) {
        if (activity != null && activity.window != null) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or flag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.window.statusBarColor = Color.TRANSPARENT
            }
        }
    }

}