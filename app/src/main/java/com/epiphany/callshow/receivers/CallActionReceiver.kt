package com.epiphany.callshow.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.epiphany.call.helpers.ACCEPT_CALL
import com.epiphany.call.helpers.DECLINE_CALL
import com.epiphany.call.manager.CallManager

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_CALL -> CallManager.accept()
            DECLINE_CALL -> CallManager.reject()
        }
    }
}
