package com.epiphany.callshow.common.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.*

/**
 * 检查权限的工具类
 */
object PermissionsChecker {
    /**
     * 判读权限集合是否缺少权限。
     *
     * @param context
     * @param permissions
     * @return 缺少权限返回true, 否则返回false。
     */
    @JvmStatic
    fun lacksPermissions(context: Context?, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (lacksPermission(context, permission)) {
                return true
            }
        }
        return false
    }

    /**
     * 判断是否缺少权限
     *
     * @param context
     * @param permission
     * @return 缺少权限返回true, 否则返回false。
     */
    @JvmStatic
    fun lacksPermission(context: Context?, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context!!, permission) ==
                PackageManager.PERMISSION_DENIED
    }

    /**
     * 过滤出未授权的权限集。
     *
     * @param context
     * @param permissions
     * @return 缺少权限返回所缺少权限的集合String[], 否则返回null。
     */
    @JvmStatic
    fun getLackPermission(context: Context?, vararg permissions: String): Array<String>? {
        val lackPermissions: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (lacksPermission(context, permission)) {
                lackPermissions.add(permission)
            }
        }
        return if (lackPermissions.size == 0) {
            null
        } else lackPermissions.toTypedArray()
    }

    /**
     * 判读权限结果是否都已授权
     *
     * @param grantResults
     * @return
     */
    @JvmStatic
    fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }

    /**
     * sdk是否6.0（>=23）或以上
     *
     * @return
     */
    @JvmStatic
    fun isOverMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= 23
    }
}