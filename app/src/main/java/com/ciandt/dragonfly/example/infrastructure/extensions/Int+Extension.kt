package com.ciandt.dragonfly.example.infrastructure.extensions

import android.content.res.Resources

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.hoursToSeconds(): Int = this * 3600

fun Int.minutesToSeconds(): Int = this * 60
