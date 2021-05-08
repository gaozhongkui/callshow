package com.epiphany.call.manager

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile
import com.epiphany.call.extensions.getCallContactsCursor
import com.epiphany.call.helpers.CallContactsContentProvider
import com.epiphany.call.helpers.SimpleContactsHelper
import com.epiphany.call.helpers.ensureBackgroundThread
import com.epiphany.call.models.CallContact

object CallManager {
    var call: Call? = null

    @SuppressLint("StaticFieldLeak")
    var inCallService: InCallService? = null
    fun accept() {
        call?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hold() {
        call?.hold()
    }

    fun unHold() {
        call?.unhold()
    }

    fun reject() {
        call?.apply {
            if (state == Call.STATE_RINGING) {
                reject(false, null)
            } else {
                disconnect()
            }
        }
    }

    fun registerCallback(callback: Call.Callback) {
        call?.registerCallback(callback)
    }

    fun unregisterCallback(callback: Call.Callback) {
        call?.unregisterCallback(callback)
    }

    fun getState() = if (call == null) {
        Call.STATE_DISCONNECTED
    } else {
        call!!.state
    }

    fun keypad(c: Char) {
        call?.apply {
            playDtmfTone(c)
            stopDtmfTone()
        }
    }

    fun getCallContact(context: Context, callback: (CallContact?) -> Unit) {
        ensureBackgroundThread {
            val callContact = CallContact("", "", "")
            if (call == null || call!!.details == null || call!!.details!!.handle == null) {
                callback(callContact)
                return@ensureBackgroundThread
            }

            val uri = Uri.decode(call!!.details.handle.toString())
            if (uri.startsWith("tel:")) {
                val number = uri.substringAfter("tel:")
                callContact.number = number
                callContact.name = SimpleContactsHelper(context).getNameFromPhoneNumber(number)
                callContact.photoUri = SimpleContactsHelper(context).getPhotoUriFromPhoneNumber(number)

                if (callContact.name != callContact.number) {
                    callback(callContact)
                } else {
                    Handler(Looper.getMainLooper()).post {
                        val privateCursor = context.getCallContactsCursor(false, true)?.loadInBackground()
                        ensureBackgroundThread {
                            val privateContacts = CallContactsContentProvider.getSimpleContacts(context, privateCursor)
                            val privateContact = privateContacts.firstOrNull { it.doesContainPhoneNumber(callContact.number) }
                            if (privateContact != null) {
                                callContact.name = privateContact.name
                            }
                            callback(callContact)
                        }
                    }
                }
            }
        }
    }

}