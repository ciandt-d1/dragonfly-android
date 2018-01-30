package com.ciandt.dragonfly.base.ui;

import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.infrastructure.ClassificationConfig;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyClassificationException;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import android.net.Uri;

import java.util.List;
import java.util.Map;

/**
 * Created by iluz on 6/9/17.
 */

public interface ClassificatorInteractor {

    void setClassificationCallbacks(LensClassificatorInteractorCallbacks classificationCallbacks);

    void setClassificationConfig(ClassificationConfig classificationConfig);

    void loadModel(Model model);

    void releaseModel();

    void analyzeFromUri(Uri uri);

    void analyzeYuvNv21Frame(byte[] data, int width, int height, int rotation);

    interface LensClassificatorInteractorCallbacks {

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void onUriAnalyzed(Uri uri, DragonflyClassificationInput classificationInput, Map<String, List<Classifier.Classification>> classifications);

        void onUriAnalysisFailed(Uri uri, DragonflyClassificationException e);

        void onYuvNv21Analyzed(Map<String, List<Classifier.Classification>> results);

        void onYuvNv21AnalysisFailed(DragonflyClassificationException e);
    }
}