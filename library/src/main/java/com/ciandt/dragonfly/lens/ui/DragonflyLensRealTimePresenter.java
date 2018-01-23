package com.ciandt.dragonfly.lens.ui;

import com.ciandt.dragonfly.base.ui.AbstractPresenter;
import com.ciandt.dragonfly.base.ui.ClassificatorInteractor;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.infrastructure.ClassificationConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyClassificationException;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import android.net.Uri;
import android.support.v4.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyLensRealTimePresenter extends AbstractPresenter<DragonflyLensRealTimeContract.LensRealTimeView> implements DragonflyLensRealTimeContract.LensRealTimePresenter, ClassificatorInteractor.LensClassificatorInteractorCallbacks, DragonflyLensRealTimeContract.LensSnapshotInteractor.SnapshotCallbacks {

    private static final String LOG_TAG = DragonflyLensRealTimePresenter.class.getSimpleName();

    private float confidenceThreshold = 0f;

    private ClassificatorInteractor lensClassificatorInteractor;
    private DragonflyLensRealTimeContract.LensSnapshotInteractor snapshotInteractor;

    private Model loadedModel;

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
    public void attachView(DragonflyLensRealTimeContract.LensRealTimeView view) {
        super.attachView(view);

        if (loadedModel != null) {
            view.onModelReady(loadedModel);
        }
    }

    @Override
    public void setClassificationConfig(ClassificationConfig classificationConfig) {
        lensClassificatorInteractor.setClassificationConfig(classificationConfig);
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
    public void onUriAnalyzed(Uri uri, DragonflyClassificationInput classificationInput, Map<String, List<Classifier.Classification>> classifications) {
        if (!hasViewAttached()) {
            return;
        }

        view.onUriAnalyzed(uri, classificationInput, classifications);
    }

    @Override
    public void onUriAnalysisFailed(Uri uri, DragonflyClassificationException e) {
        if (!hasViewAttached()) {
            return;
        }

        view.onUriAnalysisFailed(uri, e);
    }

    @Override
    public void onYuvNv21Analyzed(Map<String, List<Classifier.Classification>> classifications) {
        if (!hasViewAttached()) {
            return;
        }

        view.setLastClassifications(classifications);

        LinkedList<Pair<String, Integer>> labels = new LinkedList<>();

        for (Map.Entry<String, List<Classifier.Classification>> entry : classifications.entrySet()) {

            List<Classifier.Classification> list = entry.getValue();
            if (list == null || list.size() == 0) {
                labels.add(Pair.create("", 0));
                continue;
            }

            Classifier.Classification mainResult = list.get(0);

            if (!mainResult.hasTitle()) {
                labels.add(Pair.create("", 0));
                continue;
            }

            labels.add(Pair.create(mainResult.getTitle(), formatConfidence(mainResult.getConfidence())));
        }

        view.setLabels(labels);
    }

    @Override
    public void onYuvNv21AnalysisFailed(DragonflyClassificationException e) {
        if (!hasViewAttached()) {
            return;
        }

        view.onYuvNv21AnalysisFailed(e);
    }

    @Override
    public void onModelReady(Model model) {
        loadedModel = model;

        if (hasViewAttached()) {
            view.onModelReady(model);
        }
    }

    @Override
    public void onModelFailure(DragonflyModelException e) {
        if (hasViewAttached()) {
            view.onModelLoadFailure(e);
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
