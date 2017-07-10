package com.ciandt.dragonfly.example.infrastructure.extensions

import android.view.View

fun View.hideSoftInputView() {
    context.getInputMethodService().hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
}