package com.ciandt.dragonfly.lens.ui;

import android.net.Uri;

import com.ciandt.dragonfly.base.ui.AbstractPresenter;
import com.ciandt.dragonfly.base.ui.ClassificatorInteractor;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.infrastructure.DragonflyConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyLensRealTimePresenter extends AbstractPresenter<DragonflyLensRealTimeContract.LensRealTimeView> implements DragonflyLensRealTimeContract.LensRealTimePresenter, ClassificatorInteractor.LensClassificatorInteractorCallbacks, DragonflyLensRealTimeContract.LensSnapshotInteractor.SnapshotCallbacks {

    private static final String LOG_TAG = DragonflyLensRealTimePresenter.class.getSimpleName();

    private float confidenceThreshold = 0f;

    private ClassificatorInteractor lensClassificatorInteractor;
    private DragonflyLensRealTimeContract.LensSnapshotInteractor snapshotInteractor;

    private Model loadedModel;

    private int modelLoadingAttempts = 0;

    public DragonflyLensRealTimePresenter(ClassificatorInteractor lensClassificatorInteractor, DragonflyLensRealTimeContract.LensSnapshotInteractor snapshotInteractor) {
        if (lensClassificatorInteractor == null) {
            throw new IllegalArgumentException("lensClassificatorInteractor can't be null.");
        }

        if (snapshotInteractor == null) {
            throw new IllegalArgumentException("snapshotInteractor can't be null.");
        }

        lensClassificatorInteractor.setClassificationCallbacks(this);
        this.lensClassificatorInteractor = lensClassificatorInteractor;

        snapshotInteractor.setCallbacks(this);
        this.snapshotInteractor = snapshotInteractor;
    }

    @SuppressWarnings("unused")
    public DragonflyLensRealTimePresenter(ClassificatorInteractor lensClassificatorInteractor, DragonflyLensRealTimeContract.LensSnapshotInteractor snapshotInteractor, float confidenceThreshold) {
        this(lensClassificatorInteractor, snapshotInteractor);

        if (confidenceThreshold < 0 || confidenceThreshold > 1) {
            throw new IllegalArgumentException("confidenceThreshold should be a float between 0 and 1.");
        }

        this.lensClassificatorInteractor = lensClassificatorInteractor;
        this.confidenceThreshold = confidenceThreshold;
    }

    @Override
    public void loadModel(Model model) {
        if (model == null) {
            DragonflyLogger.warn(LOG_TAG, "loadModel() called with null argument.");
            return;
        }

        if (model.equals(this.loadedModel)) {
            DragonflyLogger.info(LOG_TAG, "This loadedModel is already currently setup. Ignoring it.");

            if (hasViewAttached()) {
                view.onModelReady(model);
            }

            return;
        }

        if (hasViewAttached()) {
            view.onStartLoadingModel(model);
        }

        modelLoadingAttempts = 0;
        lensClassificatorInteractor.loadModel(model);
    }

    @Override
    public void unloadModel() {
        loadedModel = null;
        lensClassificatorInteractor.releaseModel();
    }

    @Override
    public void analyzeFromUri(Uri uri) {
        DragonflyLogger.debug(LOG_TAG, "analyzeFromUri()");

        lensClassificatorInteractor.analyzeFromUri(uri);
    }

    @Override
    public void analyzeYuvNv21Frame(byte[] data, int width, int height, int rotation) {
        DragonflyLogger.debug(LOG_TAG, "analyzeYuvNv21Frame()");

        lensClassificatorInteractor.analyzeYuvNv21Frame(data, width, height, rotation);
    }

    @Override
    public void onUriAnalyzed(Uri uri, DragonflyClassificationInput classificationInput, List<Classifier.Recognition> classifications) {
        if (!hasViewAttached()) {
            return;
        }

        view.onUriAnalyzed(uri, classificationInput, classifications);
    }

    @Override
    public void onUriAnalysisFailed(Uri uri, DragonflyRecognitionException e) {
        if (!hasViewAttached()) {
            return;
        }

        view.onUriAnalysisFailed(uri, e);
    }

    @Override
    public void onYuvNv21Analyzed(List<Classifier.Recognition> classifications) {
        if (!hasViewAttached()) {
            return;
        }

        view.setLastClassifications(classifications);

        if (classifications == null || classifications.size() == 0) {
            view.setLabel("");
            return;
        }

        Classifier.Recognition mainResult = classifications.get(0);

        if (!mainResult.hasTitle()) {
            view.setLabel("");
            return;
        }

        if (mainResult.isRelevant(confidenceThreshold)) {
            view.setLabel(mainResult.getTitle(), formatConfidence(mainResult.getConfidence()));
            return;
        }

        view.setLabel(mainResult.getTitle());
    }

    @Override
    public void onYuvNv21AnalysisFailed(DragonflyRecognitionException e) {
        if (!hasViewAttached()) {
            return;
        }

        view.onYuvNv21AnalysisFailed(e);
    }

    @Override
    public void onModelReady(Model model) {
        if (!hasViewAttached()) {
            return;
        }

        loadedModel = model;
        view.onModelReady(model);
    }

    @Override
    public void onModelFailure(DragonflyModelException e) {
        if (e.getModel() != null) {
            if (modelLoadingAttempts == DragonflyConfig.getMaxModelLoadingRetryAttempts()) {
                String message = String.format("Failed to load loadedModel %s after %s attempts", loadedModel, modelLoadingAttempts);
                throw new DragonflyModelException(message, e, e.getModel());
            }

            DragonflyLogger.warn(LOG_TAG, String.format("Failed to load loadedModel. Retrying with %s", e.getModel()));
            modelLoadingAttempts++;
            lensClassificatorInteractor.loadModel(e.getModel());
        } else {
            if (hasViewAttached()) {
                view.onModelLoadFailure(e);
            }
        }
    }

    @Override
    public void takeSnapshot() {
        view.captureCameraFrame();
    }

    @Override
    public void onSnapshotCaptured(byte[] data, int width, int height, int rotation) {
        snapshotInteractor.saveSnapshot(data, width, height, rotation);
    }

    @Override
    public void onFailedToCaptureCameraFrame(DragonflySnapshotException e) {
        view.onSnapshotError(e);
    }

    @Override
    public void onSnapshotSaved(DragonflyClassificationInput snapshot) {
        view.onSnapshotTaken(snapshot);
    }

    @Override
    public void onFailedToSaveSnapshot(DragonflySnapshotException e) {
        view.onSnapshotError(e);
    }

    private int formatConfidence(float confidence) {
        return Math.round(confidence * 100);
    }
}
