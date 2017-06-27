package com.ciandt.dragonfly.example.infrastructure.extensions

/**
 * Created by iluz on 6/26/17.
 */

fun String.lastSegment(separator: String): String {
    val parts = split(separator)
    return parts[parts.size - 1]
}