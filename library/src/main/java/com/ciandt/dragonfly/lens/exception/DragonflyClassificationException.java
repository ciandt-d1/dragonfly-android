package com.ciandt.dragonfly.lens.exception;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyClassificationException extends RuntimeException {

    public DragonflyClassificationException(Throwable cause) {
        super(cause);
    }

    public DragonflyClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
