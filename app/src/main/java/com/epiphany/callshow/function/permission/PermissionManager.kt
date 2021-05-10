package com.epiphany.callshow.function.permission

import android.Manifest
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import com.epiphany.call.extensions.checkPermissionEnable


//检查权限管理类
object PermissionManager {


//    private var mAutoStart by SpBooleanSaver("common", "permission_auto_start")

    //检查权限
    fun checkPermission(context: Context): Boolean {
//        if (!checkAutoStart()) return false
        if (!context.checkPermissionEnable(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return false
        if (!context.checkPermissionEnable(Manifest.permission.READ_CONTACTS)) return false
        if (!checkWriteSystem(context)) return false
        if (!checkDefPhoneApp(context)) return false
        if (!checkNotificationEnable(context)) return false
        return true
    }

    //检查必须要有的权限
    fun checkNecessaryPermission(context: Context): Boolean {
        if (!context.checkPermissionEnable(Manifest.permission.WRITE_EXTERNAL_STORAGE)) return false
        if (!context.checkPermissionEnable(Manifest.permission.READ_CONTACTS)) return false
        if (!checkWriteSystem(context)) return false
        if (!checkDefPhoneApp(context)) return false
        return true
    }


    // 是否在上层显示  todo 兼容性测试

    private fun checkAlertAndDrawOver(context: Context): Boolean {
        return when (Build.VERSION.SDK_INT) {

            in 0..Build.VERSION_CODES.M -> true
            in Build.VERSION_CODES.N..Build.VERSION_CODES.O -> {
                var result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else {
                    false
                }
                if (!result) {
                    try {
                        val manager =
                                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        val params = WindowManager.LayoutParams(
                                0,
                                0,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                PixelFormat.TRANSPARENT
                        )
                        val viewToAdd = View(context)
                        viewToAdd.layoutParams = params
                        manager.addView(viewToAdd, params)
                        manager.removeView(viewToAdd)
                    } catch (e: Exception) {
                        result = false
                    }
                }

                result
            }
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }
/*
    fun checkAutoStart(): Boolean {
        return mAutoStart
    }

    //保存状态
    fun saveAutoStartToEnable() {
        mAutoStart = true
    }*/

    fun checkWriteSystem(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            return false
        }
    }

    //
    fun checkDefPhoneApp(context: Context): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mTelecom: TelecomManager =
                    context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val packageName = context.packageName
            return mTelecom.defaultDialerPackage == packageName
        }
        return false

    }


    fun checkNotificationEnable(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }


}