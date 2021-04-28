package com.epiphany.callshow.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.WindowManager
import com.epiphany.callshow.BuildConfig

internal object DialogUtil {
    private val DEBUG = BuildConfig.DEBUG
    private val TAG = if (DEBUG) "DialogUtil" else ""

    /**
     * 展示弹框
     */
    @JvmStatic
    fun showDialog(dialog: Dialog?): Boolean {
        if (DEBUG) {
            Log.d(TAG, "showDialog() called with: dialog = [$dialog]")
        }
        return try {
            if (dialog == null) {
                return false
            }
            if (dialog.window == null) {
                return false
            }
            val appDialog =
                dialog.window!!.attributes.type == WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            if (appDialog) {
                var context: Context? = dialog.context
                if (context is ContextWrapper) {
                    context = context.baseContext
                }
                if (context !is Activity) {
                    return false
                }
                if (context.isFinishing) {
                    return false
                }
            }
            if (dialog.isShowing) {
                return false
            }
            dialog.show()
            true
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * 销毁弹框
     */
    @JvmStatic
    fun dismissDialog(dialog: Dialog?) {
        if (DEBUG) {
            Log.d(TAG, "dismissDialog() called with: dialog = [$dialog]")
        }
        try {
            if (dialog == null) {
                return
            }
            val appDialog = dialog.window!!.attributes.type ==
                    WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            if (appDialog) {
                var context: Context? = dialog.context
                if (context is ContextWrapper) {
                    context = context.baseContext
                }
                if (context is Activity && context.isFinishing) {
                    return
                }
            }
            if (dialog.isShowing) {
                dialog.dismiss()
                destroy(dialog)
            }
        } catch (ignored: Throwable) {
        }
    }

    private fun destroy(dialog: Dialog?) {
        if (DEBUG) {
            Log.d(TAG, "destroy() called with: dialog = [$dialog]")
        }
        if (null != dialog) {
            dialog.setOnDismissListener(null)
            dialog.setOnShowListener(null)
            dialog.setOnKeyListener(null)
            dialog.setOnCancelListener(null)
        }
    }
}