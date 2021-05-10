package com.epiphany.callshow.function.permission

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * 授权页面权限检查实体
 * @param isAuth true:已经授权
 * @param iconRes 左侧icon资源文件
 * @param content 内容
 * @param intents 跳转系统Intent
 */
data class PermissionStateBean(
        var isAuth: Boolean,
        @DrawableRes val iconRes: Int,
        @StringRes val content: Int,
        val necessaryNeed: Boolean = false,//是否是必须要的权限，不给不行的
        val handle: ((Context) -> Unit)//异常执行方法
) {
}