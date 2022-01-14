package com.epiphany.callshow.function.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.epiphany.callshow.function.home.HomeFragment
import com.epiphany.callshow.function.my.PersonalFragment

class MainFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val mFragmentMap = hashMapOf<Int, Fragment>()
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        var fragment = mFragmentMap[position]

        fragment?.let {
            return it
        }
        fragment = when (position) {
            0 -> HomeFragment()
            1 -> PersonalFragment()
            else -> PersonalFragment()
        }

        return fragment
    }


}