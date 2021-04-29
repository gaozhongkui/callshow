package com.epiphany.callshow.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.epiphany.callshow.R

/**
 * 设置功能进度弹框
 */
class SettingFunProgressDialog(cxt: Context) : Dialog(cxt, R.style.transparent_dialog_style) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        setContentView(R.layout.dialog_setting_fun_progress)
    }

    companion object {
        /**
         * 展示Loading弹框
         */
        fun showLoadingDialog(activity: Activity): SettingFunProgressDialog {
            val dialog = SettingFunProgressDialog(activity)
            DialogUtil.showDialog(dialog)
            return dialog
        }

    }
}