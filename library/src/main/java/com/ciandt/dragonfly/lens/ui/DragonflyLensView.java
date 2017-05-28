package com.ciandt.dragonfly.lens.ui;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Size;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ciandt.dragonfly.CameraView;
import com.ciandt.dragonfly.R;
import com.ciandt.dragonfly.data.Model;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;

/**
 * Created by iluz on 5/22/17.
 */

public class DragonflyLensView extends FrameLayout implements DragonflyLensContract.LensView, CameraView.LensViewCallback {

    private static final String LOG_TAG = DragonflyLensView.class.getSimpleName();

    private Model model;

    private TextView labelView;
    private CameraView cameraView;

    private DragonflyLensContract.LensPresenter lensPresenter;

    @Override
    public void setModel(Model model) {
        this.model = model;

        lensPresenter.setupModel(model);
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
        initialize(context);
    }

    public DragonflyLensView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public DragonflyLensView(Context context,
                             AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context the current context for the view.
     */
    private void initialize(Context context) {
        DragonflyLogger.debug(LOG_TAG, "initialize()");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.dragonfly_lens_view, this);

        lensPresenter = new DragonflyLensPresenter(new DragonflyLensInteractor(getContext()));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        labelView = (TextView) this.findViewById(R.id.labelView);
        cameraView = (CameraView) this.findViewById(R.id.cameraView);
    }


    public void start() {
        lensPresenter.attach(this);
        startCameraView();
    }

    public void stop() {
        lensPresenter.detach();
        stopCameraView();
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
    public void onFrameReady(byte[] data, Size previewSize) {
        lensPresenter.analyzeYUVNV21(data, previewSize.getWidth(), previewSize.getHeight());
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
                return createFromParcel(null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}