package com.epiphany.callshow.function.splash

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import com.epiphany.callshow.R
import com.epiphany.callshow.common.base.BaseActivity
import com.epiphany.callshow.common.base.BaseViewModel
import com.epiphany.callshow.common.utils.StatusBarUtil
import com.epiphany.callshow.databinding.ActivitySplashLayoutBinding
import com.epiphany.callshow.extensions.launch
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
    }

    override fun onStart() {
        super.onStart()
        binding.lottieView.playAnimation()
    }

    private fun jumpMainAct() {
        launch(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TOP)
        finish()
    }
}