package com.epiphany.call.services

import android.telecom.Call
import android.telecom.InCallService
import com.epiphany.call.manager.CallManager
import java.lang.Exception

class CallService : InCallService() {
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
    /*    try {
            val intent = PhoneCallActivity.getIntent(this)
            App.getApp().startActivity(intent)
        } catch (e: Exception) {
            ExternalActivityManager.getInstance(App.getApp())
                .startExternalActivity(PhoneCallActivity.getIntent(this))
        }*/


        CallManager.call = call
        CallManager.inCallService = this
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.call = null
        CallManager.inCallService = null
    }
}