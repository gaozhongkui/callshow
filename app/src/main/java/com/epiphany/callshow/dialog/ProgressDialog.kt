package com.epiphany.callshow.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.airbnb.lottie.LottieAnimationView
import com.epiphany.callshow.R
import com.epiphany.callshow.common.utils.SystemInfo

class ProgressDialog(cxt: Context) : Dialog(cxt, R.style.transparent_dialog_style) {
    private var mLottieAnimationView: LottieAnimationView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        setContentView(R.layout.dialog_progress)
        mLottieAnimationView = findViewById(R.id.la_animation_view)
    }

    override fun show() {
        super.show()
        mLottieAnimationView?.playAnimation()
    }

    override fun dismiss() {
        super.dismiss()
        mLottieAnimationView?.pauseAnimation()
    }


    companion object {
        /**
         * 展示Loading弹框
         */
        fun showLoadingDialog(activity: Activity): ProgressDialog? {
            if (!SystemInfo.isValidActivity(activity)) {
                return null
            }
            val progressDialog = ProgressDialog(activity)
            try {
                progressDialog.show()
                return progressDialog
            } catch (e: Throwable) {

            }
            return null
        }
    }

}