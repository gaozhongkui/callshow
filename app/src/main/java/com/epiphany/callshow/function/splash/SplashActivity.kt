package com.epiphany.callshow.function.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.databinding.ActivitySplashLayoutBinding
import com.epiphany.callshow.function.main.MainActivity

class SplashActivity : BaseActivity<BaseViewModel, ActivitySplashLayoutBinding>() {
    override fun getBindLayout(): Int = R.layout.activity_splash_layout

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun initView() {
        StatusBarUtil.setTranslucentStatus(this, true)
        binding.lottieView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                jumpMainAct()
            }
        })
        binding.lottieView.playAnimation()
    }

    private fun jumpMainAct() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}