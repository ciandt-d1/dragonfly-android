package com.ciandt.dragonfly.feedback.ui;

import com.ciandt.dragonfly.R;
import com.ciandt.dragonfly.base.ui.ImageScaleTypes;
import com.ciandt.dragonfly.image_processing.ImageUtils;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.ui.DragonflyLabelView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * Created by iluz on 6/9/17.
 */

public class DragonflyLensFeedbackView extends RelativeLayout {

    private static final String LOG_TAG = DragonflyLensFeedbackView.class.getSimpleName();

    private LinearLayout labelsContainer;
    private ImageView previewView;
    private ImageView ornamentView;

    public DragonflyLensFeedbackView(Context context) {
        super(context);
        initialize(context, null);
    }

    public DragonflyLensFeedbackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public DragonflyLensFeedbackView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    public void setClassificationInput(DragonflyClassificationInput classificationInput) {
        if (classificationInput == null || TextUtils.isEmpty(classificationInput.getImagePath())) {
            previewView.setImageDrawable(null);
            return;
        }

        Bitmap bitmap = ImageUtils.loadBitmapFromDisk(classificationInput.getImagePath());
        previewView.setImageBitmap(bitmap);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context the current context for the view.
     */
    private void initialize(Context context, AttributeSet attrs) {
        DragonflyLogger.debug(LOG_TAG, "initialize()");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dragonfly_lens_feedback_view, this);

        labelsContainer = (LinearLayout) this.findViewById(R.id.dragonflyLensLabelsContainer);

        previewView = (ImageView) this.findViewById(R.id.previewImageView);
        ornamentView = (ImageView) this.findViewById(R.id.dragonflyLensOrnamentView);

        processAttributeSet(context, attrs);
    }

    private void processAttributeSet(Context context, AttributeSet attrs) {
        DragonflyLogger.debug(LOG_TAG, "processAttributeSet()");

        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragonflyLensFeedbackView, 0, 0);
        try {
            Drawable ornamentDrawable = typedArray.getDrawable(R.styleable.DragonflyLensFeedbackView_dlfvCameraOrnament);
            setOrnamentDrawable(ornamentDrawable);

            final int scaleTypeIndex = typedArray.getInt(R.styleable.DragonflyLensFeedbackView_dlfvCameraOrnamentScaleType, -1);
            if (scaleTypeIndex >= 0 && scaleTypeIndex <= ImageScaleTypes.VALUES.length) {
                setOrnamentScaleType(ImageScaleTypes.VALUES[scaleTypeIndex]);
                ornamentView.setScaleType(ImageScaleTypes.VALUES[scaleTypeIndex]);
            }
        } finally {
            typedArray.recycle();
        }
    }

    public void setOrnamentDrawable(Drawable ornamentDrawable) {
        if (ornamentDrawable != null) {
            ornamentView.setImageDrawable(ornamentDrawable);
            ornamentView.setVisibility(VISIBLE);
        } else {
            ornamentView.setVisibility(GONE);
        }
    }

    public void setOrnamentScaleType(ImageView.ScaleType scaleType) {
        ornamentView.setScaleType(scaleType);
    }

    public void setLabels(List<Pair<String, Integer>> labels) {

        for (int i = 0; i < labels.size(); i++) {
            DragonflyLabelView labelView = (DragonflyLabelView) labelsContainer.getChildAt(i);
            if (labelView == null) {
                labelView = new DragonflyLabelView(getContext());
                labelsContainer.addView(labelView);
            }

            Pair<String, Integer> info = labels.get(i);
            labelView.setInfo(info.first, getContext().getString(R.string.percentage_label, info.second));
        }

        for (int i = labels.size(); i < labelsContainer.getChildCount(); i++) {
            labelsContainer.removeViewAt(i);
        }

        labelsContainer.setVisibility(VISIBLE);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            //noinspection unchecked
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            //noinspection unchecked
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    static class SavedState extends BaseSavedState {

        SparseArray childrenStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            //noinspection unchecked
            out.writeSparseArray(childrenStates);
        }

        public static final ClassLoaderCreator<SavedState> CREATOR
                = new ClassLoaderCreator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(source, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}