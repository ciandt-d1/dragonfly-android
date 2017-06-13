package com.ciandt.dragonfly.lens.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ciandt.dragonfly.R;
import com.ciandt.dragonfly.base.ui.ImageScaleTypes;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.io.File;
import java.util.List;

/**
 * Created by iluz on 6/9/17.
 */

public class DragonflyLensFeedbackView extends FrameLayout implements DragonflyLensFeedbackContract.FeedbackView {

    private static final String LOG_TAG = DragonflyLensFeedbackView.class.getSimpleName();

    private ImageView previewView;
    private ImageView ornamentView;

    private DragonflyLensFeedbackContract.FeedbackPresenter feedbackPresenter;


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

    @Override
    public void onModelReady(Model model) {
        // TODO: define if we should use this info locally (avoid calling the presenter while the model is not ready?)
    }

    @Override
    public void onModelFailure(DragonflyModelException e) {
        // TODO: handle the error in a user friendly way.
    }

    @Override
    public void onBitmapAnalyzed(List<Classifier.Recognition> results) {

    }

    @Override
    public void onBitmapAnalysisFailed(DragonflyRecognitionException e) {
        // TODO: handle the error in a user friendly way.
    }

    @Override
    public void setSnapshot(DragonflyCameraSnapshot snapshot) {
        if (snapshot == null || TextUtils.isEmpty(snapshot.getPath())) {
            previewView.setImageDrawable(null);
            return;
        }

        File imgFile = new File(snapshot.getPath());
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            previewView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void setModel(Model model) {
        loadModel(model);
        feedbackPresenter.attach(this);
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

        previewView = (ImageView) this.findViewById(R.id.previewImageView);
        ornamentView = (ImageView) this.findViewById(R.id.ornamentView);

        feedbackPresenter = new DragonflyFeedbackPresenter(new DragonflyLensClassificatorInteractor(getContext()));

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
            if (ornamentDrawable != null) {
                ornamentView.setImageDrawable(ornamentDrawable);
                ornamentView.setVisibility(VISIBLE);
            } else {
                ornamentView.setVisibility(GONE);
            }

            final int scaleTypeIndex = typedArray.getInt(R.styleable.DragonflyLensFeedbackView_dlfvCameraOrnamentScaleType, -1);
            if (scaleTypeIndex >= 0 && scaleTypeIndex <= ImageScaleTypes.VALUES.length) {
                ornamentView.setScaleType(ImageScaleTypes.VALUES[scaleTypeIndex]);
            }
        } finally {
            typedArray.recycle();
        }
    }

    private void loadModel(Model model) {
        DragonflyLogger.debug(LOG_TAG, String.format("%s.loadModel(%s)", LOG_TAG, model));

        feedbackPresenter.loadModel(model);
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