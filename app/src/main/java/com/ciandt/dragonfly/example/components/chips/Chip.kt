package com.ciandt.dragonfly.example.components.chips

interface Chip {

    fun getText(): String

    fun isSelected(): Boolean

    fun setSelected(status: Boolean)
}