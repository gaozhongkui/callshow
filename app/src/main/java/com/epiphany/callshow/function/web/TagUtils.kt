package com.epiphany.callshow.function.web

import android.text.TextUtils

object TagUtils {
    private fun getTagByUrl(url: String): String {
        if (url.contains("qq")) {
            return if (url.contains("iframe")) {
                //(全屏视频。通过网页【分享】- 【通用代码】)
                // https://v.qq.com/iframe/player.html?vid=m0394wjagsq&tiny=0&auto=0
                "tvp_fullscreen_button"
            } else {
                // 普通网页界面
                // https://v.qq.com/x/page/m0394wjagsq.html
                "txp_btn_fullscreen"
            }
        } else if (url.contains("bilibili")) {
            return "icon-widescreen" // http://www.bilibili.com/mobile/index.html
        }
        return ""
    }

    //  "javascript:document.getElementsByClassName('" + referParser(url) + "')[0].addEventListener('click',function(){local_obj.playing();return false;});"
    fun getJs(url: String): String {
        val tag = getTagByUrl(url)
        return if (TextUtils.isEmpty(tag)) {
            "javascript:"
        } else {
            "javascript:document.getElementsByClassName('$tag')[0].addEventListener('click',function(){onClickFullScreenBtn.fullscreen();return false;});"
        }
    }
}