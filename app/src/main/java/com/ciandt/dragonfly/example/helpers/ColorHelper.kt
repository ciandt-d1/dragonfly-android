package com.ciandt.dragonfly.example.helpers

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object ColorHelper {

    fun toGradient(fromColor: String, toColor: String): GradientDrawable {

        val from = Color.parseColor(fromColor)
        val to = Color.parseColor(toColor)

        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(from, to))
        gradientDrawable.cornerRadius = 0f

        return gradientDrawable
    }
}
