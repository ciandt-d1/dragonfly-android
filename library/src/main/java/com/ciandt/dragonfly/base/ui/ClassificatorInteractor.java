package com.ciandt.dragonfly.base.ui;

import android.graphics.Bitmap;

import com.ciandt.dragonfly.data.model.Model;
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

    void analyzeBitmap(Bitmap bitmap);

    void analyzeYUVNV21Picture(byte[] data, int width, int height, int rotation);

    interface LensClassificatorInteractorCallbacks {

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void onImageAnalyzed(List<Classifier.Recognition> results);

        void onImageAnalysisFailed(DragonflyRecognitionException e);
    }
}