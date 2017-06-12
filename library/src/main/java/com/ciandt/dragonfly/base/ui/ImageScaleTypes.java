package com.ciandt.dragonfly.base.ui;

import android.widget.ImageView;

/**
 * Created by iluz on 6/12/17.
 */

public interface ImageScaleTypes {

    ImageView.ScaleType[] VALUES = {
            ImageView.ScaleType.MATRIX,
            ImageView.ScaleType.FIT_XY,
            ImageView.ScaleType.FIT_START,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };
}
