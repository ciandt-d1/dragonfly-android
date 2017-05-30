package com.ciandt.dragonfly.lens.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciandt.dragonfly.CameraView;
import com.ciandt.dragonfly.R;
import com.ciandt.dragonfly.base.ui.Orientation;
import com.ciandt.dragonfly.base.ui.Size;
import com.ciandt.dragonfly.data.Model;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;

/**
 * Created by iluz on 5/22/17.
 */

public class DragonflyLensView extends FrameLayout implements DragonflyLensContract.LensView, CameraView.LensViewCallback {

    private static final String LOG_TAG = DragonflyLensView.class.getSimpleName();

    @Orientation.Mode
    private int orientation;

    private TextView labelView;
    private CameraView cameraView;
    private ImageView ornamentView;

    private DragonflyLensContract.LensPresenter lensPresenter;

    private static final ImageView.ScaleType[] SCALE_TYPES = {
            ImageView.ScaleType.MATRIX,
            ImageView.ScaleType.FIT_XY,
            ImageView.ScaleType.FIT_START,
            ImageView.ScaleType.FIT_CENTER,
            ImageView.ScaleType.FIT_END,
            ImageView.ScaleType.CENTER,
            ImageView.ScaleType.CENTER_CROP,
            ImageView.ScaleType.CENTER_INSIDE
    };


    private void loadModel(Model model) {
        DragonflyLogger.debug(LOG_TAG, String.format("%s.loadModel(%s)", LOG_TAG, model));

        lensPresenter.loadModel(model);
    }

    @Override
    public void setLabel(String label) {
        labelView.setVisibility(TextUtils.isEmpty(label) ? GONE : VISIBLE);

        String formattedLabel = getContext().getString(R.string.label_without_confidence, label);

        labelView.setText(formattedLabel);
    }

    @Override
    public void setLabel(String label, int confidence) {
        labelView.setVisibility(TextUtils.isEmpty(label) ? GONE : VISIBLE);

        String formattedLabel = getContext().getString(R.string.label_with_confidence, label, confidence);

        labelView.setText(formattedLabel);
    }

    @Override
    public void setOrientation(@Orientation.Mode int orientation) {
        this.orientation = orientation;
    }

    @Override
    public void onModelReady(Model model) {
        // TODO: define if we should use this info locally (avoid calling the presenter while the model is not ready?)
    }

    @Override
    public void onModelFailure(DragonflyModelException e) {
        // TODO: handle the error in a user friendly way.
    }

    @Override
    public void onBitmapAnalysisFailed(DragonflyRecognitionException e) {
        // TODO: handle the error in a user friendly way.
    }

    public DragonflyLensView(Context context) {
        super(context);
        initialize(context, null);
    }

    public DragonflyLensView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public DragonflyLensView(Context context,
                             AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
        initialize(context, attrs);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context the current context for the view.
     */
    private void initialize(Context context, AttributeSet attrs) {
        DragonflyLogger.debug(LOG_TAG, "initialize()");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dragonfly_lens_view, this);

        labelView = (TextView) this.findViewById(R.id.labelView);

        cameraView = (CameraView) this.findViewById(R.id.cameraView);
        cameraView.setOrientation(orientation);

        ornamentView = (ImageView) this.findViewById(R.id.ornamentView);

        lensPresenter = new DragonflyLensPresenter(new DragonflyLensInteractor(getContext()));

        processAttributeSet(context, attrs);
    }

    private void processAttributeSet(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragonflyLensView, 0, 0);
        try {
            Drawable ornamentDrawable = typedArray.getDrawable(R.styleable.DragonflyLensView_cameraOrnament);
            if (ornamentDrawable != null) {
                ornamentView.setImageDrawable(ornamentDrawable);
                ornamentView.setVisibility(VISIBLE);
            }

            final int scaleTypeIndex = typedArray.getInt(R.styleable.DragonflyLensView_cameraOrnamentScaleType, -1);
            if (scaleTypeIndex >= 0 && scaleTypeIndex <= SCALE_TYPES.length) {
                ornamentView.setScaleType(SCALE_TYPES[scaleTypeIndex]);
            }
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    public void start(Model model) {
        loadModel(model);
        lensPresenter.attach(this);
        startCameraView();

        // Not sure why, but this guarantees the camera works after turning the screen off and then
        // back on.
        cameraView.setVisibility(VISIBLE);
    }

    @Override
    public void stop() {
        lensPresenter.detach();
        stopCameraView();

        cameraView.setVisibility(GONE);
    }

    private void startCameraView() {
        try {
            cameraView.start();
            cameraView.setCallback(this);
        } catch (Exception e) {
            DragonflyLogger.error(LOG_TAG, e.getMessage(), e);
        }
    }

    private void stopCameraView() {
        try {
            cameraView.setCallback(null);
            cameraView.stop();
        } catch (Exception e) {
            DragonflyLogger.error(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onFrameReady(byte[] data, Size previewSize, int rotation) {
        lensPresenter.analyzeYUVNV21(data, previewSize.getWidth(), previewSize.getHeight(), rotation);
    }

    @Override
    public void onPreviewStarted(Size previewSize, int rotation) {

    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
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