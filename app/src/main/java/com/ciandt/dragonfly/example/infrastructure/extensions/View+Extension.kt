package com.ciandt.dragonfly.example.infrastructure.extensions

import android.view.View

fun View.hideSoftInputView() {
    context.getInputMethodService().hideSoftInputFromWindow(windowToken, 0)
    clearFocus()
}

fun View.isVisible() = View.VISIBLE == this.visibility

fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}
