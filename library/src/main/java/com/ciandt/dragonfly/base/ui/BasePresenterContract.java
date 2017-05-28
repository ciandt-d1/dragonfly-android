package com.ciandt.dragonfly.base.ui;

/**
 * Created by iluz on 5/22/17.
 */

public interface BasePresenterContract<V> {

    void attach(V view);

    void detach();

    boolean hasViewAttached();
}
