package com.epiphany.callshow.common.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseDialogFragment<VM : ViewModel, Binding : ViewDataBinding>/**/() :
        DialogFragment() {

    protected lateinit var binding: Binding
    protected lateinit var viewModel: VM

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        viewModel = ViewModelProvider(this).get(getViewModelClass())
        initView()
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        applyDialog(dialog)
        return dialog
    }


    protected abstract fun applyDialog(dialog: Dialog)

    abstract fun getViewModelClass(): Class<VM>

    protected abstract fun initView()

    protected abstract fun getLayoutId(): Int

    //重置大小 注意执行顺序
    protected fun resetSize(width: Int, height: Int) {
        dialog?.window?.let {
            it.decorView.setPadding(0, 0, 0, 0)
            val lp = it.attributes
            lp.width = width
            lp.height = height
        }
    }

}