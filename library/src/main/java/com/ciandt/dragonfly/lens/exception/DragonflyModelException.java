package com.ciandt.dragonfly.lens.exception;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyModelException extends Exception {

    public DragonflyModelException(Throwable cause) {
        super(cause);
    }

    public DragonflyModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
