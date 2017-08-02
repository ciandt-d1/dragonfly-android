package com.ciandt.dragonfly.example.infrastructure.extensions

import android.app.Activity
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.ViewGroup

fun Activity.getRootView(): ViewGroup {
    return window.decorView.findViewById(android.R.id.content)
}

fun Activity.showSnackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(getRootView(), text, duration).show()
}

fun Activity.showSnackbar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_LONG) {
    showSnackbar(getString(resId), duration)
}