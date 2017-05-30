package com.ciandt.dragonfly.base.ui;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by iluz on 5/29/17.
 */

public interface Orientation {

    @Retention(SOURCE)
    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_LANDSCAPE})
    @interface Mode {

    }

    int ORIENTATION_PORTRAIT = 0;
    int ORIENTATION_LANDSCAPE = 1;
}
