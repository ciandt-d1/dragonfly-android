package com.ciandt.dragonfly.base.ui;

/**
 * Created by iluz on 5/26/17.
 */
public class AbstractPresenter<V> implements BasePresenterContract<V> {

    protected V view;

    @Override
    public void attachView(V view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public boolean hasViewAttached() {
        return view != null;
    }
}
