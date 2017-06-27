package com.ciandt.dragonfly.example.shared

interface BasePresenterContract<in V> {

    fun attachView(view: V)

    fun detachView()

    fun hasViewAttached(): Boolean
}
