package com.ciandt.dragonfly.lens.exception;

/**
 * Created by iluz on 6/9/17.
 */

public class DragonflySnapshotException extends RuntimeException {

    public DragonflySnapshotException(Throwable cause) {
        super(cause);
    }

    public DragonflySnapshotException(String message, Throwable cause) {
        super(message, cause);
    }
}