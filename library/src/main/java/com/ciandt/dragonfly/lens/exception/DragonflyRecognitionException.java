package com.ciandt.dragonfly.lens.exception;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyRecognitionException extends RuntimeException {

    public DragonflyRecognitionException(Throwable cause) {
        super(cause);
    }

    public DragonflyRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
