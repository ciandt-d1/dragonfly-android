package com.ciandt.dragonfly.lens.ui;

import android.graphics.Bitmap;

import com.ciandt.dragonfly.base.ui.BasePresenterContract;
import com.ciandt.dragonfly.base.ui.BaseViewContract;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 6/9/17.
 */

public interface DragonflyLensFeedbackContract {

    interface FeedbackView extends BaseViewContract {

        void start(Model model);

        void stop();

        void setSnapshot(DragonflyCameraSnapshot snapshot);

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void analyzeSnapshot();

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
