package com.ciandt.dragonfly.lens.exception;

import com.ciandt.dragonfly.data.Model;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyModelException extends RuntimeException {

    private Model model;

    public DragonflyModelException(String message, Throwable cause) {
        super(message, cause);
    }

    public DragonflyModelException(String message, Throwable cause, Model model) {
        super(message, cause);

        this.model = model;
    }

    public Model getModel() {
        return model;
    }

}
