package com.ciandt.dragonfly.example.helpers

import java.text.DecimalFormat
import java.text.NumberFormat

object SizeHelper {

    fun toReadable(size: Long, format: NumberFormat = DecimalFormat.getNumberInstance(), units: Array<String> = arrayOf("B", "kB", "MB", "GB", "TB", "PB")): String {
        if (size <= 0) return "0"
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        if (digitGroups >= units.size) {
            return size.toString()
        }
        return format.format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

}
