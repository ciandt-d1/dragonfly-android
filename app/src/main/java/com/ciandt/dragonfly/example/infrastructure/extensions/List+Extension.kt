package com.ciandt.dragonfly.example.infrastructure.extensions

fun <E> MutableList<E>.clearAndAddAll(elements: Collection<E>): Boolean {
    clear()
    return addAll(elements)
}

fun <T> List<T>.head(): T {
    return first()
}

fun <T> List<T>.tail(): List<T> {
    if (size > 1) {
        return drop(1)
    }
    return emptyList()
}

