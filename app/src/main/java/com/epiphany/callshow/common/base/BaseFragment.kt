package com.epiphany.callshow.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseFragment<T : ViewModel, S : ViewDataBinding> : Fragment() {
    protected lateinit var binding: S
    protected lateinit var viewModel: T

    abstract fun getBindLayout(): Int

    abstract fun getViewModelClass(): Class<T>

    abstract fun initView()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, getBindLayout(), container, false)
        viewModel = ViewModelProvider(this.activity!!).get(getViewModelClass())
        initView()
        return binding.root
    }
}