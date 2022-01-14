package com.epiphany.callshow.function.my

import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.databinding.FragmentPersonalLayoutBinding

class PersonalFragment : BaseFragment<BaseViewModel, FragmentPersonalLayoutBinding>() {
    override fun getBindLayout(): Int {
        return R.layout.fragment_personal_layout
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {

    }
}