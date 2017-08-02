package com.ciandt.dragonfly.example.helpers

import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object ColorHelper {

    fun toGradient(fromColor: String, toColor: String, fromDefault: Int = Color.TRANSPARENT, toDefault: Int = Color.TRANSPARENT): GradientDrawable {

        val from = parseColor(fromColor, fromDefault)
        val to = parseColor(toColor, toDefault)

        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(from, to))
        gradientDrawable.cornerRadius = 0f

        return gradientDrawable
    }

    fun parseColor(colorString: String, default: Int = Color.TRANSPARENT): Int {
        try {
            return Color.parseColor(colorString)
        } catch (e: Exception) {
            return default
        }
    }
}
