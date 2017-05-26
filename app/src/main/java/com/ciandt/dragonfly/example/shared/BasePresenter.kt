package com.ciandt.dragonfly.example.shared

interface BasePresenter<V> {

    fun attachView(view: V)

    fun detachView()
}
