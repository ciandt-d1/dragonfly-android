package com.ciandt.dragonfly.base.ui;

import android.net.Uri;

import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 6/9/17.
 */

public interface ClassificatorInteractor {

    void setClassificationCallbacks(LensClassificatorInteractorCallbacks classificationCallbacks);

    void loadModel(Model model);

    void releaseModel();

    void analyzeFromUri(Uri uri);

    void analyzeYuvNv21Frame(byte[] data, int width, int height, int rotation);

    interface LensClassificatorInteractorCallbacks {

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void onUriAnalyzed(Uri uri, DragonflyClassificationInput classificationInput, List<Classifier.Recognition> classifications);

        void onUriAnalysisFailed(Uri uri, DragonflyRecognitionException e);

        void onYuvNv21Analyzed(List<Classifier.Recognition> results);

        void onYuvNv21AnalysisFailed(DragonflyRecognitionException e);
    }
}