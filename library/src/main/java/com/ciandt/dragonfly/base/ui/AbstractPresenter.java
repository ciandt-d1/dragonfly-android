package com.ciandt.dragonfly.base.ui;

/**
 * Created by iluz on 5/26/17.
 */
public class AbstractPresenter<V> implements BasePresenterContract<V> {

    protected V view;

    @Override
    public void attach(V view) {
        this.view = view;
    }

    @Override
    public void detach() {
        view = null;
    }

    @Override
    public boolean hasViewAttached() {
        return view != null;
    }
}
