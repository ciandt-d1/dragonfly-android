package com.ciandt.dragonfly.lens.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciandt.dragonfly.CameraView;
import com.ciandt.dragonfly.R;
import com.ciandt.dragonfly.base.ui.ImageScaleTypes;
import com.ciandt.dragonfly.base.ui.Orientation;
import com.ciandt.dragonfly.base.ui.Size;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.infrastructure.DragonflyConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.infrastructure.PermissionsMapping;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyClassificationException;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 5/22/17.
 */

public class DragonflyLensRealtimeView extends FrameLayout implements DragonflyLensRealTimeContract.LensRealTimeView, CameraView.LensViewCallback {

    private static final String LOG_TAG = DragonflyLensRealtimeView.class.getSimpleName();

    @Orientation.Mode
    private int orientation;

    private TextView labelView;
    private CameraView cameraView;
    private ImageView ornamentView;
    private ImageButton btnSnapshot;

    private ProgressDialog progressDialogModelLoading;
    private ProgressDialog progressDialogUriAnalysis;

    private DragonflyLensRealTimeContract.LensRealTimePresenter lensRealTimePresenter;

    private ModelCallbacks modelCallbacks;
    private SnapshotCallbacks snapshotCallbacks;
    private PermissionsCallback permissionsCallback;
    private UriAnalysisCallbacks uriAnalysisCallbacks;

    private List<Classifier.Classification> lastClassifications;

    public DragonflyLensRealtimeView(Context context) {
        super(context);
        initialize(context, null);
    }

    public DragonflyLensRealtimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public DragonflyLensRealtimeView(Context context,
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
        inflater.inflate(R.layout.dragonfly_lens_realtime_view, this);

        labelView = (TextView) this.findViewById(R.id.dragonflyLensLabelView);

        cameraView = (CameraView) this.findViewById(R.id.dragonflyLensCameraView);
        cameraView.setOrientation(orientation);

        ornamentView = (ImageView) this.findViewById(R.id.dragonflyLensOrnamentView);

        btnSnapshot = (TakePhotoButton) this.findViewById(R.id.dragonflyLensBtnSnapshot);
        btnSnapshot.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (lastClassifications == null || lastClassifications.isEmpty()) {
                    Toast.makeText(getContext(), R.string.lens_no_classifications_available, Toast.LENGTH_SHORT).show();
                } else {
                    new MediaActionSound().play(MediaActionSound.SHUTTER_CLICK);
                    lensRealTimePresenter.takeSnapshot();
                }
            }
        });

        lensRealTimePresenter = new DragonflyLensRealTimePresenter(new DragonflyLensClassificatorInteractor(getContext()), new DragonflyLensSnapshotInteractor(getContext()));

        processAttributeSet(context, attrs);
    }

    private void processAttributeSet(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DragonflyLensRealtimeView, 0, 0);
        try {
            Drawable ornamentDrawable = typedArray.getDrawable(R.styleable.DragonflyLensRealtimeView_dlvCameraOrnament);
            setOrnamentDrawable(ornamentDrawable);

            final int scaleTypeIndex = typedArray.getInt(R.styleable.DragonflyLensRealtimeView_dlvCameraOrnamentScaleType, -1);
            if (scaleTypeIndex >= 0 && scaleTypeIndex <= ImageScaleTypes.VALUES.length) {
                setOrnamentScaleType(ImageScaleTypes.VALUES[scaleTypeIndex]);
            }
        } finally {
            typedArray.recycle();
        }
    }

    public void setOrnamentDrawable(Drawable ornamentDrawable) {
        ornamentView.setImageDrawable(ornamentDrawable);
    }

    public void setOrnamentScaleType(ImageView.ScaleType scaleType) {
        ornamentView.setScaleType(scaleType);
    }

    public void setModelCallbacks(ModelCallbacks callbacks) {
        this.modelCallbacks = callbacks;
    }

    public void setSnapshotCallbacks(SnapshotCallbacks snapshotCallbacks) {
        this.snapshotCallbacks = snapshotCallbacks;
    }

    public void setPermissionsCallback(PermissionsCallback permissionsCallback) {
        this.permissionsCallback = permissionsCallback;
    }

    public void setUriAnalysisCallbacks(UriAnalysisCallbacks uriAnalysisCallbacks) {
        this.uriAnalysisCallbacks = uriAnalysisCallbacks;
    }

    public void analyzeFromUri(Uri uri) {
        showUriAnalysisProgress();
        lensRealTimePresenter.analyzeFromUri(uri);
    }

    @Override
    public List<Classifier.Classification> getLastClassifications() {
        return lastClassifications;
    }

    @Override
    public void setLastClassifications(List<Classifier.Classification> classifications) {
        lastClassifications = classifications;
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


    private void hideLabel() {
        labelView.setVisibility(GONE);
    }

    private void hideControls() {
        btnSnapshot.setVisibility(GONE);
        hideLabel();
    }

    public void showControls(boolean animateControls) {
        makeViewVisible(btnSnapshot, animateControls);
    }

    @Override
    public void setOrientation(@Orientation.Mode int orientation) {
        this.orientation = orientation;
    }

    @Override
    public void onStartLoadingModel(Model model) {
        if (modelCallbacks != null) {
            modelCallbacks.onStartLoadingModel(model);
        }
    }

    @Override
    public void onModelReady(Model model) {
        hideModelLoadingProgress(true);

        if (modelCallbacks != null) {
            modelCallbacks.onModelReady(model);
        }

        if (ornamentView.getDrawable() != null) {
            makeViewVisible(ornamentView, true);
        }
    }

    private void makeViewVisible(View view, boolean animate) {
        if (View.VISIBLE == view.getVisibility()) {
            return;
        }

        if (animate) {
            long duration = DragonflyConfig.getRealTimeControlsVisibilityAnimationDuration();

            view.setAlpha(0f);
            view.setVisibility(VISIBLE);
            view.animate()
                    .alpha(1.0f)
                    .setDuration(duration);
        } else {
            view.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onModelLoadFailure(DragonflyModelException e) {
        if (modelCallbacks != null) {
            modelCallbacks.onModelLoadFailure(e);
        }
    }

    @Override
    public void onUriAnalyzed(Uri uri, DragonflyClassificationInput classificationInput, List<Classifier.Classification> classifications) {
        if (uriAnalysisCallbacks != null) {
            uriAnalysisCallbacks.onUriAnalysisFinished(uri, classificationInput, classifications);
        }

        hideUriAnalysisProgress(false);
    }

    @Override
    public void onUriAnalysisFailed(Uri uri, DragonflyClassificationException e) {
        if (uriAnalysisCallbacks != null) {
            uriAnalysisCallbacks.onUriAnalysisFailed(e);
        }

        hideUriAnalysisProgress(false);
    }

    @Override
    public void onYuvNv21AnalysisFailed(DragonflyClassificationException e) {
        // TODO: handle the error in a user friendly way.
    }

    @Override
    public void captureCameraFrame() {
        if (permissionsCallback == null) {
            throw new IllegalStateException("setPermissionsCallback() should be called with a valid PermissionsCallback instance");
        }

        if (permissionsCallback.checkPermissions(PermissionsMapping.CAPTURE_FRAME)) {
            cameraView.takeSnapshot();
        }
    }

    @Override
    public void onStartTakingSnapshot() {
        if (snapshotCallbacks != null) {
            snapshotCallbacks.onStartTakingSnapshot();
        }
    }

    @Override
    public void onSnapshotTaken(DragonflyClassificationInput snapshot) {
        if (snapshotCallbacks != null) {
            snapshotCallbacks.onSnapshotTaken(snapshot);
        }
    }

    @Override
    public void onSnapshotError(DragonflySnapshotException e) {
        if (snapshotCallbacks != null) {
            snapshotCallbacks.onSnapshotError(e);
        }
    }

    @Override
    public void start() {
        lensRealTimePresenter.attachView(this);

        startCameraView();

        // Not sure why, but this guarantees the camera works after turning the screen off and then
        // back on.
        cameraView.setVisibility(VISIBLE);
    }

    @Override
    public void stop() {
        lensRealTimePresenter.detachView();
        stopCameraView();

        cameraView.setVisibility(GONE);

        hideModelLoadingProgress(false);
        hideUriAnalysisProgress(false);
    }

    @Override
    public void loadModel(Model model) {
        DragonflyLogger.debug(LOG_TAG, String.format("%s.loadModel(%s)", LOG_TAG, model));

        showModelLoadingProgress();

        lensRealTimePresenter.loadModel(model);
    }

    @Override
    public void unloadModel() {
        DragonflyLogger.debug(LOG_TAG, String.format("%s.unloadModel", LOG_TAG));

        lensRealTimePresenter.unloadModel();
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
        lensRealTimePresenter.analyzeYuvNv21Frame(data, previewSize.getWidth(), previewSize.getHeight(), rotation);
    }

    @Override
    public void onSnapshotCaptured(byte[] data, Size previewSize, int rotation) {
        lensRealTimePresenter.onSnapshotCaptured(data, previewSize.getWidth(), previewSize.getHeight(), rotation);
    }

    @Override
    public void onPreviewStarted(Size previewSize, int rotation) {

    }

    private void showModelLoadingProgress() {
        if (progressDialogModelLoading != null) {
            return;
        }

        progressDialogModelLoading = new ProgressDialog(getContext());
        progressDialogModelLoading.setMessage(getContext().getString(R.string.lens_loading_message_loading_model));
        progressDialogModelLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogModelLoading.setIndeterminate(true);
        progressDialogModelLoading.setCancelable(false);
        progressDialogModelLoading.show();

        hideControls();
    }

    private void hideModelLoadingProgress(boolean animateControls) {
        if (progressDialogModelLoading != null) {
            progressDialogModelLoading.hide();
            progressDialogModelLoading = null;
        }

        showControls(animateControls);
    }

    private void showUriAnalysisProgress() {
        if (progressDialogUriAnalysis != null) {
            return;
        }

        progressDialogUriAnalysis = new ProgressDialog(getContext());
        progressDialogUriAnalysis.setMessage(getContext().getString(R.string.lens_loading_message_classifying_image));
        progressDialogUriAnalysis.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialogUriAnalysis.setIndeterminate(true);
        progressDialogUriAnalysis.setCancelable(false);
        progressDialogUriAnalysis.show();

        hideControls();
    }

    private void hideUriAnalysisProgress(boolean animateControls) {
        if (progressDialogUriAnalysis != null) {
            progressDialogUriAnalysis.hide();
            progressDialogUriAnalysis = null;
        }

        showControls(animateControls);
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

    public interface ModelCallbacks {

        void onStartLoadingModel(Model model);

        void onModelReady(Model model);

        void onModelLoadFailure(DragonflyModelException e);
    }

    public interface SnapshotCallbacks {

        void onStartTakingSnapshot();

        void onSnapshotTaken(DragonflyClassificationInput snapshot);

        void onSnapshotError(DragonflySnapshotException e);
    }

    public interface PermissionsCallback {

        boolean checkPermissions(List<String> permissions);
    }

    public interface UriAnalysisCallbacks {

        void onUriAnalysisFinished(Uri uri, DragonflyClassificationInput classificationInput, List<Classifier.Classification> classifications);

        void onUriAnalysisFailed(DragonflyClassificationException e);
    }
}