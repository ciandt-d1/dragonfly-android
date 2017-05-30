package com.ciandt.dragonfly.example.shared

/**
 * Created by iluz on 5/30/17.
 */
abstract class BasePresenter<V> : BasePresenterContract<V> {
    protected var view: V? = null

    override fun attachView(view: V) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override fun hasViewAttached(): Boolean {
        return view != null
    }
}