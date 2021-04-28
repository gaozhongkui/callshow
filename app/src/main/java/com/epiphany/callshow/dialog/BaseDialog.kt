package com.epiphany.callshow.dialog

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.epiphany.callshow.R
import com.epiphany.callshow.databinding.DialogBaseBinding
import com.epiphany.callshow.dialog.DialogUtil.dismissDialog
import com.epiphany.callshow.dialog.DialogUtil.showDialog

abstract class BaseDialog(private var mContext: Context) {
    protected var mDialog: AlertDialog? = AlertDialog.Builder(mContext).create()
    protected var mBinding: DialogBaseBinding
    protected var dimAmount: Float
        protected get() = 0.70f
        protected set(aimAmount) {
            if (mDialog!!.window != null) {
                mDialog!!.window!!.setDimAmount(dimAmount)
            }
        }
    protected val isFullScreen: Boolean
        protected get() = false

    protected fun onDialogShow(dialog: DialogInterface?) {}
    protected abstract fun setContentView(parent: ViewGroup?): View
    protected fun interceptBack() {
        mDialog!!.setOnKeyListener { dialog, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
    }

    open fun canceledOnTouchOutside(): Boolean {
        return true
    }

    fun show() {
        if (mDialog != null) {
            showDialog(mDialog)
            fixSize()
        }
    }

    private fun fixSize() {
        if (mDialog == null || mContext is Activity && (mContext as Activity).isFinishing) {
            return
        }
        val window = mDialog!!.window ?: return
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        window.attributes = lp
        window.decorView.setPadding(0, 0, 0, 0)
        window.setBackgroundDrawable(ColorDrawable())
        if (dialogAnimator != 0) {
            mDialog!!.window!!.setWindowAnimations(dialogAnimator)
        }
    }

    protected val dialogAnimator: Int
        protected get() = R.style.customDialogAnim

    fun setDismissListener(dismissListener: DialogInterface.OnDismissListener?) {
        mDialog!!.setOnDismissListener(dismissListener)
    }

    val isShowing: Boolean
        get() = mDialog != null && mDialog!!.isShowing

    fun dismiss() {
        if (mDialog != null && mDialog!!.isShowing) {
            try {
                dismissDialog(mDialog)
            } catch (ignore: IllegalStateException) {
                //dismissInternal(true);
                //Can not perform this action after onSaveInstanceState
                //Activity is finishing
            } catch (ignore: IllegalArgumentException) {
            }
        }
    }

    init {
        if (mDialog!!.window != null) {
            mDialog!!.window!!.setDimAmount(dimAmount)
            if (isFullScreen) {
                mDialog!!.window!!.setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
        mDialog!!.setOnShowListener { dialog -> onDialogShow(dialog) }
        mDialog!!.setCanceledOnTouchOutside(canceledOnTouchOutside())
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.dialog_base, null, false)
        mBinding.shadow.setOnClickListener {
            if (canceledOnTouchOutside()) {
                dismiss()
            }
        }
        mBinding.container.setOnClickListener { }
        val contentView = setContentView(mBinding.container)
        mBinding.container.addView(contentView)
        mDialog!!.setView(mBinding.root)
        mDialog!!.setCanceledOnTouchOutside(false)
    }
}