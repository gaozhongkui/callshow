package com.epiphany.callshow.function.personal

import android.os.Bundle
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseFragment
import com.epiphany.callshow.databinding.FragmentPersonalLayoutBinding

class PersonalFragment : BaseFragment<PersonalViewModel, FragmentPersonalLayoutBinding>() {
    override fun getBindLayout(): Int = R.layout.fragment_personal_layout

    override fun getViewModelClass(): Class<PersonalViewModel> {
        return PersonalViewModel::class.java
    }

    override fun initView() {

    }

    companion object {
        fun newInstance(bundle: Bundle? = null): PersonalFragment {
            val fragment = PersonalFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}