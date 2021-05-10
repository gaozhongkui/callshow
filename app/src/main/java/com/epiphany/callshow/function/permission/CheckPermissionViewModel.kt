package com.epiphany.callshow.function.permission

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.lifecycle.MutableLiveData
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.constant.REQUEST_CODE_SET_DEFAULT_DIALER

class CheckPermissionViewModel : BaseViewModel() {

    companion object {
        const val REQUEST_AUTO_START = 1
    }

    //数据源
    val infoList = MutableLiveData<MutableList<PermissionStateBean>>()

    //加载数据
    fun loadInfoList(context: Activity) {
        val result = getPermissionStateList(context)
        infoList.value = result
    }

    //获取数据
    private fun getPermissionStateList(context: Activity): MutableList<PermissionStateBean> {
        val result = mutableListOf<PermissionStateBean>()
//        result.add(
//            PermissionStateBean(
//                checkAlertAndDrawOver(context),
//                R.drawable.ic_jurisdiction_video,
//                R.string.permission_name_float_window,
//                arrayOf(RoomManager.get().getAccManagerIntent(context))
//            )
//        )

        val normalStart: (Context, Array<Intent>) -> Unit = { ctx, intents ->
            try {
                ctx.startActivities(intents)
            } catch (e: Exception) {
                //失败就跳转设置
                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + ctx.packageName)))
            }
        }

        //检查读取来电通知
        result.add(
                PermissionStateBean(PermissionManager.checkNotificationEnable(context), R.drawable.ic_jurisdiction_inform, R.string.permission_name_notification, false
                ) {
//                    normalStart.invoke(it, arrayOf(RomManager.get().getNotificationManagerIntent(context)))
                }
        )

        //保持来电秀正常显示  自启动比较复杂，需要根据国内厂商判定 （自启动，换方式实现）
//        result.add(
//                PermissionStateBean(PermissionManager.checkAutoStart(), R.drawable.ic_jurisdiction_iphone, R.string.permission_name_app_auto_start) {
//                    //dialog
//                    try {
//                        context.startActivityForResult(RomManager.get().getAutoStartIntent(context), REQUEST_AUTO_START)
//                    } catch (e: Exception) {
//                        //失败就跳转设置
//                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
//                    }
//                }
//        )

        //修改手机铃声
        result.add(
                PermissionStateBean(PermissionManager.checkWriteSystem(context), R.drawable.ic_jurisdiction_music, R.string.permission_name_revise_sys_setting, true) {
//                    normalStart.invoke(it, arrayOf(RomManager.get().getWritingSetting(context)))
                }
        )

        //替换来电界面
        result.add(
                PermissionStateBean(PermissionManager.checkDefPhoneApp(context), R.drawable.ic_jurisdiction_theme, R.string.permission_name_def_phone_app, true) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        val roleManager = context.getSystemService(RoleManager::class.java)
//                        if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
//                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
//                            context.startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
//                        }
//                    } else {
                    if (Build.MODEL == "TRT-AL00") {
                        launchToDefaultAppSetting(context)
                    } else {
                        Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName).apply {
                            try {
                                context.startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                            } catch (e: ActivityNotFoundException) {
                                try {
                                    launchToDefaultAppSetting(context)
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            } catch (e: Exception) {
                                try {
                                    launchToDefaultAppSetting(context)
                                } catch (ex: Exception) {
                                    ex.printStackTrace()
                                }
                            }
                        }
                    }
                }
        )

        return result
    }

    private fun launchToDefaultAppSetting(context: Context) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }
}