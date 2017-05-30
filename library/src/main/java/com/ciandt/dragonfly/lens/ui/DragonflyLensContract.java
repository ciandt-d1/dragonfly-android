package com.ciandt.dragonfly.lens.ui;

import android.graphics.Bitmap;

import com.ciandt.dragonfly.base.ui.BaseInteractorContract;
import com.ciandt.dragonfly.base.ui.BasePresenterContract;
import com.ciandt.dragonfly.base.ui.BaseViewContract;
import com.ciandt.dragonfly.base.ui.Orientation;
import com.ciandt.dragonfly.data.Model;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 5/22/17.
 */

public interface DragonflyLensContract {

    interface LensView extends BaseViewContract {

        void start(Model model);

        void stop();

        void setLabel(String label);

        void setLabel(String label, int confidence);

        void setOrientation(@Orientation.Mode int orientation);

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void onBitmapAnalysisFailed(DragonflyRecognitionException e);
    }

    interface LensPresenter extends BasePresenterContract<LensView> {

        void loadModel(Model model);

        void analyzeBitmap(Bitmap bitmap);

        void analyzeYUVNV21(byte[] data, int width, int height, int rotation);

        void onImageAnalyzed(List<Classifier.Recognition> results);

        void onImageAnalysisFailed(DragonflyRecognitionException e);

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);
    }

    interface LensInteractorContract extends BaseInteractorContract<LensPresenter> {

        void loadModel(Model model);

        void releaseModel();

        void analyzeBitmap(Bitmap bitmap);

        void analyzeYUVNV21Picture(byte[] data, int width, int height, int rotation);
    }
}