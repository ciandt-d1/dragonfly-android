package com.ciandt.dragonfly.lens.ui;

import android.graphics.Bitmap;

import com.ciandt.dragonfly.base.ui.AbstractPresenter;
import com.ciandt.dragonfly.data.Model;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyLensPresenter extends AbstractPresenter<DragonflyLensContract.LensView> implements DragonflyLensContract.LensPresenter {

    private static final String LOG_TAG = DragonflyLensPresenter.class.getSimpleName();

    private static final int MAX_MODEL_LOADING_ATTEMPTS = 5;

    private float confidenceThreshold = 0f;

    private DragonflyLensContract.LensInteractorContract interactor;

    private Model loadedModel;

    private int modelLoadingAttempts = 0;

    public DragonflyLensPresenter(DragonflyLensContract.LensInteractorContract interactor) {
        if (interactor == null) {
            throw new IllegalArgumentException("interactor can't be null.");
        }

        interactor.setPresenter(this);
        this.interactor = interactor;
    }

    @SuppressWarnings("unused")
    public DragonflyLensPresenter(DragonflyLensContract.LensInteractorContract interactor, float confidenceThreshold) {
        this(interactor);

        if (confidenceThreshold < 0 || confidenceThreshold > 1) {
            throw new IllegalArgumentException("confidenceThreshold should be a float between 0 and 1.");
        }

        this.interactor = interactor;
        this.confidenceThreshold = confidenceThreshold;
    }

    @Override
    public void detach() {
        super.detach();

        loadedModel = null;
        interactor.releaseModel();
    }

    @Override
    public void loadModel(Model model) {
        if (model == null) {
            DragonflyLogger.warn(LOG_TAG, "loadModel() called with null argument.");
            return;
        }

        if (model.equals(this.loadedModel)) {
            DragonflyLogger.info(LOG_TAG, "This loadedModel is already currently setup. Ignoring it.");
            return;
        }

        modelLoadingAttempts = 0;
        interactor.loadModel(model);
    }

    @Override
    public void analyzeBitmap(Bitmap bitmap) {
        interactor.analyzeBitmap(bitmap);
    }

    @Override
    public void analyzeYUVNV21(byte[] data, int width, int height) {
        interactor.analyzeYUVNV21Picture(data, width, height);
    }

    @Override
    public void onImageAnalyzed(List<Classifier.Recognition> results) {
        if (!hasViewAttached()) {
            return;
        }

        if (results == null || results.size() == 0) {
            view.setLabel("");
            return;
        }

        Classifier.Recognition mainResult = results.get(0);

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
    public void onImageAnalysisFailed(DragonflyRecognitionException e) {
        if (!hasViewAttached()) {
            return;
        }

        view.onBitmapAnalysisFailed(e);
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
            if (modelLoadingAttempts == MAX_MODEL_LOADING_ATTEMPTS) {
                String message = String.format("Failed to load loadedModel %s after %s attempts", loadedModel, modelLoadingAttempts);
                throw new DragonflyModelException(message, e, e.getModel());
            }

            DragonflyLogger.warn(LOG_TAG, String.format("Failed to load loadedModel. Retrying with %s", e.getModel()));
            modelLoadingAttempts++;
            interactor.loadModel(e.getModel());
            return;
        } else {
            if (hasViewAttached()) {
                view.onModelFailure(e);
            }
        }
    }

    private int formatConfidence(float confidence) {
        return Math.round(confidence * 100);
    }
}
