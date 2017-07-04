package com.ciandt.dragonfly.example.helpers

import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v4.graphics.drawable.DrawableCompat

class DrawableHelper {
    companion object {
        fun getTintedDrawable(drawable: Drawable, @ColorInt tint: Int): Drawable {
            val wrappedDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrappedDrawable, tint)

            return wrappedDrawable
        }
    }
}