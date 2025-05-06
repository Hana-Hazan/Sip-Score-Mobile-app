package com.example.sipscore.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = activity.currentFocus ?: View(activity)
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
