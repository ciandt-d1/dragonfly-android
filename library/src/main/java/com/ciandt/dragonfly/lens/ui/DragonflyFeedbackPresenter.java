package com.ciandt.dragonfly.lens.ui;

import android.graphics.Bitmap;

import com.ciandt.dragonfly.base.ui.AbstractPresenter;
import com.ciandt.dragonfly.base.ui.ClassificatorInteractor;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.infrastructure.DragonflyConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 6/9/17.
 */

public class DragonflyFeedbackPresenter extends AbstractPresenter<DragonflyLensFeedbackContract.FeedbackView> implements DragonflyLensFeedbackContract.FeedbackPresenter, ClassificatorInteractor.LensClassificatorInteractorCallbacks {

    private static final String LOG_TAG = DragonflyFeedbackPresenter.class.getSimpleName();

    private ClassificatorInteractor lensClassificatorInteractor;

    private Model loadedModel;

    private int modelLoadingAttempts = 0;

    public DragonflyFeedbackPresenter(ClassificatorInteractor classificatorInteractor) {
        if (classificatorInteractor == null) {
            throw new IllegalArgumentException("classificatorInteractor can't be null.");
        }

        classificatorInteractor.setClassificationCallbacks(this);
        this.lensClassificatorInteractor = classificatorInteractor;
    }

    @SuppressWarnings("unused")
    public DragonflyFeedbackPresenter(ClassificatorInteractor lensClassificatorInteractor, float confidenceThreshold) {
        this(lensClassificatorInteractor);

        if (confidenceThreshold < 0 || confidenceThreshold > 1) {
            throw new IllegalArgumentException("confidenceThreshold should be a float between 0 and 1.");
        }

        this.lensClassificatorInteractor = lensClassificatorInteractor;
    }

    @Override
    public void detachView() {
        super.detachView();

        loadedModel = null;
        lensClassificatorInteractor.releaseModel();
    }

    @Override
    public void loadModel(Model model) {
        if (model == null) {
            DragonflyLogger.warn(LOG_TAG, "loadModel() called with null argument.");
            return;
        }

        if (model.equals(this.loadedModel)) {
            DragonflyLogger.info(LOG_TAG, "This model is already currently setup. Ignoring it.");
            return;
        }

        modelLoadingAttempts = 0;
//        lensClassificatorInteractor.loadModel(model);
    }

    @Override
    public void analyzeBitmap(Bitmap bitmap) {
        lensClassificatorInteractor.analyzeBitmap(bitmap);
    }

    @Override
    public void onImageAnalyzed(List<Classifier.Recognition> results) {
        if (!hasViewAttached()) {
            return;
        }

        view.onBitmapAnalyzed(results);
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
            if (modelLoadingAttempts == DragonflyConfig.getMaxModelLoadingRetryAttempts()) {
                String message = String.format("Failed to load loadedModel %s after %s attempts", loadedModel, modelLoadingAttempts);
                throw new DragonflyModelException(message, e, e.getModel());
            }

            DragonflyLogger.warn(LOG_TAG, String.format("Failed to load loadedModel. Retrying with %s", e.getModel()));
            modelLoadingAttempts++;
            lensClassificatorInteractor.loadModel(e.getModel());
        } else {
            if (hasViewAttached()) {
                view.onModelFailure(e);
            }
        }
    }
}
