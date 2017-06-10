package com.ciandt.dragonfly.lens.ui;

import android.graphics.Bitmap;

import com.ciandt.dragonfly.base.ui.BasePresenterContract;
import com.ciandt.dragonfly.base.ui.BaseViewContract;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 5/22/17.
 */

public interface DragonflyLensFeedbackContract {

    interface FeedbackView extends BaseViewContract {

        void setModel(Model model);

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void onBitmapAnalyzed(List<Classifier.Recognition> results);

        void onBitmapAnalysisFailed(DragonflyRecognitionException e);
    }

    interface FeedbackPresenter extends BasePresenterContract<FeedbackView> {

        void loadModel(Model model);

        void analyzeBitmap(Bitmap bitmap);

        void onImageAnalyzed(List<Classifier.Recognition> results);

        void onImageAnalysisFailed(DragonflyRecognitionException e);

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);
    }
}
