package com.epiphany.callshow.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import com.epiphany.callshow.R
import com.epiphany.callshow.databinding.ActivityPermissionAuthGuideBinding

/**
 * 修复弹框
 * @param btnId 底部文案
 * @param block 底部文案点击回调
 */
class FixPermissionDialog(
    @StringRes var btnId: Int,
    var block: () -> Unit?
) :
    BaseDialogFragment() {
    private lateinit var binding: ActivityPermissionAuthGuideBinding


    override fun createView(inflater: LayoutInflater): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.activity_permission_auth_guide, null, false)
        initView(context!!)
        return binding.root
    }


    private fun initView(context: Context) {
        binding.tvDes.text = context.getString(
            R.string.permission_guide_des1,
            context.getString(context.applicationInfo.labelRes)
        )

        binding.bottom.setTextRes(btnId)
        binding.bottom.setOnClickListener {
            block.invoke()
        }
        binding.bottom.setOnClickListener(this::onDismiss)
        binding.ivBack.setOnClickListener {
            dismiss()
        }
    }

    //退出页面
    private fun onDismiss(view: View) {
        dismiss()
        block?.invoke()
    }
}