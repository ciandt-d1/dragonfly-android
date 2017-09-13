package com.ciandt.dragonfly.example.infrastructure.extensions

import android.content.res.Resources

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.hours(): Int = this * 3600

fun Int.minutes(): Int = this * 60
