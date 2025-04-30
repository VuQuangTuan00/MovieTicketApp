// File: NavigationUtils.kt
package com.example.movieticketsapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent

fun Context.navigateTo(destination: Class<*>, finishCurrent: Boolean = false) {
    val intent = Intent(this, destination)
    startActivity(intent)
    if (this is Activity && finishCurrent) {
        finish()
    }
}
