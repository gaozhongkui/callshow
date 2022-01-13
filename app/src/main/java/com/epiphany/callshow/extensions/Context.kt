package com.epiphany.callshow.extensions

import android.content.Context
import android.content.Intent

fun Context.launch(toActivity: Class<*>, flags: Int = 0) {
    val intent = Intent(this, toActivity)
    intent.flags = flags
    startActivity(intent)
}