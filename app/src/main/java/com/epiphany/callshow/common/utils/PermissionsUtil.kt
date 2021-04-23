package com.epiphany.callshow.common.utils

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.PermissionChecker
import com.epiphany.callshow.App
import com.epiphany.callshow.R
import java.util.*

/**
 * Time:21-1-8-下午7:24
 * Author:gaozhongkui
 * Description:权限工具类
 */
object PermissionsUtil {
    val TAG = "PermissionsUtil"

    fun isUsageStatsPermissionGranted(context: Context): Boolean {
        var hasPermission = false
        try {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            hasPermission = appOpsManager.checkOpNoThrow(
                    "android:get_usage_stats", Process.myUid(), context.packageName
            ) == AppOpsManager.MODE_ALLOWED
            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasPermission =
                        context.checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Exception) {
        }
        return hasPermission
    }

    fun shouldShowPolicyDialog(cxt: Context): Boolean {
        val sp: SharedPreferences = cxt.getSharedPreferences("splash", Context.MODE_PRIVATE)
        return (!sp.getBoolean("has_shown_policy", false) &&
                cxt.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.M)
    }

    fun updatePolicyDialogShownState() {
        App.getApp().apply {
            val sp: SharedPreferences = getSharedPreferences("splash", Context.MODE_PRIVATE)
            sp.edit().putBoolean("has_shown_policy", true).apply()
        }

    }


    fun isPermissionGranted(permission: String?): Boolean {
        if (App.getApp() == null) {
            return false
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionChecker.checkSelfPermission(
                    App.getApp()!!,
                    permission!!
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun getDeniedPermissions(permissions: Array<String>): List<String>? {
        val deniedPermissions: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissions.add(permission)
            }
        }
        return deniedPermissions
    }


    fun getClickableHtml(
        html: String,
        @ColorRes colorResId: Int = R.color.color_link_text,
        underLine: Boolean = false
    ): CharSequence? {

        val spannedHtml = Html.fromHtml(html)
        val clickableHtmlBuilder = SpannableStringBuilder(spannedHtml)
        val urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length, URLSpan::class.java)
        for (span in urls) {
            setLinkClickable(clickableHtmlBuilder, span, colorResId, underLine)
        }
        return clickableHtmlBuilder
    }

    private fun setLinkClickable(
            clickableHtmlBuilder: SpannableStringBuilder,
            urlSpan: URLSpan,
            @ColorRes colorResId: Int,
            underLine: Boolean
    ) {
        val start = clickableHtmlBuilder.getSpanStart(urlSpan)
        val end = clickableHtmlBuilder.getSpanEnd(urlSpan)
        val flags = clickableHtmlBuilder.getSpanFlags(urlSpan)
        val span: URLSpan = object : URLSpan(urlSpan.url) {
            override fun onClick(widget: View) {
                super.onClick(widget)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                App.getApp()?.apply {
                    ds.color = resources.getColor(colorResId)
                    ds.isUnderlineText = underLine
                }

            }
        }
        clickableHtmlBuilder.setSpan(span, start, end, flags)
    }


    private fun getPermissionGuideDesc(permissionList: List<String>): String? {
        val desc = StringBuilder()
        if (permissionList.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || permissionList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            desc.append("\"存储\"")
        }
        if (permissionList.contains(Manifest.permission.READ_PHONE_STATE)) {
            if (desc.length > 0) {
                desc.append("，")
            }
            desc.append("\"电话\"")
        }
        if (permissionList.contains(Manifest.permission.ACCESS_FINE_LOCATION)
                || permissionList.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permissionList.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            if (desc.length > 0) {
                desc.append("，")
            }
            desc.append("\"位置\"")
        }
        return desc.toString()
    }

    interface PermissionsDialogCallback {
        fun onCancel()
        fun onRequest()
        fun onGranted(permissions: List<String?>?)
        fun onDenied(permissions: List<String?>?, rationaleList: List<String?>?)
    }

    interface PolicyDialogCallback {
        fun onCancel()
        fun onConfirm()
    }

    fun checkFloatWindowPermission(context: Context?): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= 23) {
            if (Build.MANUFACTURER.toLowerCase() == "vivo") {
                val status: Int = PermissionsUtil.getFloatPermissionStatus(context)
                Log.i(TAG, "PermissionsUtil ViVo getFloatPermissionStatus: $status")
                if (status >= 0) {
                    return status == 0
                }
            }
            try {
                val clazz: Class<*> = Settings::class.java
                val canDrawOverlays = clazz.getDeclaredMethod(
                        "canDrawOverlays",
                        Context::class.java
                )
                result = canDrawOverlays.invoke(null, context) as Boolean
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    /**
     * 获取悬浮窗权限状态 适配ViVo设备比较旧的系统
     *
     * @param context context
     * @return -1是有异常或者拿不到具体信息, 1或其他是没有打开，0是打开，该状态的定义和[android.app.AppOpsManager.MODE_ALLOWED]，MODE_IGNORED等值差不多，自行查阅源码
     */
    fun getFloatPermissionStatus(context: Context?): Int {
        if (context == null) {
            return -1
        }
        val packageName = context.packageName
        val uri = Uri.parse("content://com.iqoo.secure.provider.secureprovider/allowfloatwindowapp")
        val selection = "pkgname = ?"
        val selectionArgs = arrayOf(packageName)
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver
                    .query(uri, null, selection, selectionArgs, null)
            if (cursor != null) {
                cursor.columnNames
                if (cursor.moveToFirst()) {
                    cursor.getInt(cursor.getColumnIndex("currentlmode"))
                } else {
                    getFloatPermissionStatus2(context)
                }
            } else {
                getFloatPermissionStatus2(context)
            }
        } catch (e: java.lang.Exception) {
            getFloatPermissionStatus2(context)
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed) {
                    cursor.close()
                }
            }
        }
    }

    /**
     * 获取悬浮窗权限状态 适配ViVo设备比较新的系统
     *
     * @param context context
     */
    private fun getFloatPermissionStatus2(context: Context?): Int {
        if (context == null) {
            return -1
        }
        val packageName = context.packageName
        val uri2 =
                Uri.parse("content://com.vivo.permissionmanager.provider.permission/float_window_apps")
        val selection = "pkgname = ?"
        val selectionArgs = arrayOf(packageName)
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver
                    .query(uri2, null, selection, selectionArgs, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    cursor.getInt(cursor.getColumnIndex("currentmode"))
                } else {
                    -1
                }
            } else {
                -1
            }
        } catch (e: java.lang.Exception) {
            -1
        } finally {
            if (cursor != null) {
                if (!cursor.isClosed) {
                    cursor.close()
                }
            }
        }
    }

    fun applyFloatWindowPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= 23) {
            var intent: Intent? = null
            if (Build.MANUFACTURER.toLowerCase() == "vivo") {
                try {
                    intent = Intent("permission.intent.action.softPermissionDetail")
                    intent.putExtra("packagename", context.packageName)
                    context.startActivity(intent)
                } catch (t: Throwable) {
                    intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(intent)
                }
                return
            }
            try {
                intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
            } catch (ignore: Throwable) {
            }
        }
    }

    fun isOverlayGranted(context: Context?): Boolean {
        var ret = true
        if (Build.VERSION.SDK_INT >= 23) {
            ret = Settings.canDrawOverlays(context)
        }
        return ret
    }

    fun hasPIPFeature(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else false
    }

    fun checkPIPPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    context.applicationInfo.uid, context.packageName
            )
        } else false
    }


}