package com.ciandt.dragonfly.example.infrastructure.extensions

import android.content.Context
import android.view.LayoutInflater

fun Context.getLayoutInflaterService(): LayoutInflater {
    return getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}