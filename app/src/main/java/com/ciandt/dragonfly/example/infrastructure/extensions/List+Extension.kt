package com.ciandt.dragonfly.example.infrastructure.extensions

fun <T> List<T>.head(): T {
    return first()
}

fun <T> List<T>.tail(): List<T> {
    if (size > 1) {
        return drop(1)
    }
    return emptyList()
}

fun <E> MutableList<E>.clearAndAddAll(elements: Collection<E>): Boolean {
    clear()
    return addAll(elements)
}

fun <E> MutableList<E>.replace(element: E): Boolean {
    val index = indexOf(element)
    if (index == -1) {
        return false
    }
    add(index, element)
    return true
}