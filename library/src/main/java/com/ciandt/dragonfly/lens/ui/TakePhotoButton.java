package com.ciandt.dragonfly.lens.ui;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.ciandt.dragonfly.R;

/**
 * Created by iluz on 6/13/17.
 */
public class TakePhotoButton extends android.support.v7.widget.AppCompatImageButton {

    private int iconPadding = 8;

    public TakePhotoButton(@NonNull Context context) {
        this(context, null, 0);
    }

    public TakePhotoButton(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TakePhotoButton(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setSoundEffectsEnabled(false);
        setIconPadding(iconPadding);

        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.take_photo_button));
        setIconPadding(iconPadding);
    }

    private void setIconPadding(int paddingInDp) {
        Resources resources = getResources();
        int paddingInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingInDp, resources.getDisplayMetrics());

        setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx);
    }
}
