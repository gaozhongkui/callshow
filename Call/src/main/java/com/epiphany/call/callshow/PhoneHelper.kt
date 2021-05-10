package com.epiphany.call.callshow

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import android.text.TextUtils
import androidx.fragment.app.Fragment
import com.epiphany.call.helpers.REQUEST_CODE_SET_DEFAULT_DIALER
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import java.util.*

/**
 * 手机号辅助类
 */
object PhoneHelper {
    private const val LANGUAGE = "CN"
    private val mPhoneNumberUtil = PhoneNumberUtil.getInstance()
    private val mCarrierMapper: PhoneNumberToCarrierMapper =
        PhoneNumberToCarrierMapper.getInstance()
    private val mGeocoder: PhoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance()


    /**
     * 获取手机运营商
     */
    fun getCarrier(phoneNumber: String): String {
        var referencePhoneNumber = Phonenumber.PhoneNumber()
        try {
            referencePhoneNumber = mPhoneNumberUtil.parse(phoneNumber, LANGUAGE)
        } catch (e: NumberParseException) {

        }
        //返回结果只有英文，自己转成成中文
        val carrierEn: String =
            mCarrierMapper.getNameForNumber(referencePhoneNumber, Locale.ENGLISH)
        var carrierZh = ""
        return if (TextUtils.equals(Locale.CHINA.country, Locale.getDefault().country)) {
            when (carrierEn) {
                "China Mobile" -> carrierZh += "移动"
                "China Unicom" -> carrierZh += "联通"
                "China Telecom" -> carrierZh += "电信"
                else -> {
                }
            }
            carrierZh
        } else {
            carrierEn
        }
    }

    /**
     * 获取手机号码归属地
     */
    fun getQCellCore(phoneNumber: String?): String? {
        var referencePhoneNumber: Phonenumber.PhoneNumber? = null
        try {
            referencePhoneNumber = mPhoneNumberUtil.parse(phoneNumber, LANGUAGE)
        } catch (e: NumberParseException) {
        }

        referencePhoneNumber?.apply {
            //手机号码归属城市 referenceRegion
            return mGeocoder.getDescriptionForNumber(referencePhoneNumber, Locale.CHINA)
        }
        return null
    }

    /**
     * 设置默认电话权限
     */
    fun setDefaultPhoneAppSetting(fragment: Fragment) {
        fragment.context?.let { cxt ->
            if (Build.MODEL == "TRT-AL00") {
                launchToDefaultAppSetting(cxt)
            } else {
                Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                    cxt.packageName
                ).apply {
                    try {
                        fragment.startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                    } catch (e: Exception) {
                        try {
                            launchToDefaultAppSetting(cxt)
                        } catch (ex: Exception) {
                        }
                    }
                }
            }
        }

    }

    private fun launchToDefaultAppSetting(context: Context) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
    }
}