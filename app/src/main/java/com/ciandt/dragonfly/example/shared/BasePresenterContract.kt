package com.ciandt.dragonfly.example.shared

interface BasePresenterContract<V> {

    fun attachView(view: V)

    fun detachView()

    fun hasViewAttached(): Boolean
}
