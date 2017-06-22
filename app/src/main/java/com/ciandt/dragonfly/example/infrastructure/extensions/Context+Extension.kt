package com.ciandt.dragonfly.example.infrastructure.extensions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager


fun Context.getLayoutInflaterService(): LayoutInflater {
    return getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}

fun Context.getInputMethodService(): InputMethodManager {
    return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}

fun Context.hideSoftInputView(view: View?) {
    view?.let {
        getInputMethodService().hideSoftInputFromWindow(it.windowToken, 0)
        it.clearFocus()
    }
}