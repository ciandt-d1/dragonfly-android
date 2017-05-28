package com.ciandt.dragonfly.base.ui;

/**
 * Created by iluz on 5/26/17.
 */

public interface BaseInteractorContract<P> {

    void setPresenter(P presenter);

    static class AsyncTaskResult<R, E> {

        private R result;
        private E error;

        public AsyncTaskResult(R result, E error) {
            this.result = result;
            this.error = error;
        }

        public R getResult() {
            return result;
        }

        public E getError() {
            return error;
        }

        public boolean hasError() {
            return error != null;
        }
    }
}
