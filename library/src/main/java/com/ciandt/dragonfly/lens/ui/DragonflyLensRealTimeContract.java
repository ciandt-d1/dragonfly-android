package com.ciandt.dragonfly.lens.ui;

import android.net.Uri;
import android.support.annotation.StringRes;

import com.ciandt.dragonfly.base.ui.BaseInteractorContract;
import com.ciandt.dragonfly.base.ui.BasePresenterContract;
import com.ciandt.dragonfly.base.ui.BaseViewContract;
import com.ciandt.dragonfly.base.ui.Orientation;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import java.util.List;

/**
 * Created by iluz on 5/22/17.
 */

public interface DragonflyLensRealTimeContract {

    interface LensRealTimeView extends BaseViewContract {

        void start();

        void stop();

        void loadModel(Model model);

        void unloadModel();

        void showLoading(@StringRes int messageRes);

        void hideLoading(boolean animateControls);

        List<Classifier.Recognition> getLastClassifications();

        void setLastClassifications(List<Classifier.Recognition> classifications);

        void setLabel(String label);

        void setLabel(String label, int confidence);

        void hideLabel();

        void setOrientation(@Orientation.Mode int orientation);

        void onStartLoadingModel(Model model);

        void onModelReady(Model model);

        void onModelLoadFailure(DragonflyModelException e);

        void onUriAnalyzed(Uri uri, DragonflyClassificationInput classificationInput, List<Classifier.Recognition> classifications);

        void onUriAnalysisFailed(Uri uri, DragonflyRecognitionException e);

        void onYuvNv21AnalysisFailed(DragonflyRecognitionException e);

        void captureCameraFrame();

        void onStartTakingSnapshot();

        void onSnapshotTaken(DragonflyClassificationInput snapshot);

        void onSnapshotError(DragonflySnapshotException e);
    }

    interface LensRealTimePresenter extends BasePresenterContract<LensRealTimeView> {

        void loadModel(Model model);

        void unloadModel();

        void analyzeFromUri(Uri uri);

        void analyzeYuvNv21Frame(byte[] data, int width, int height, int rotation);

        void takeSnapshot();

        void onSnapshotCaptured(byte[] data, int width, int height, int rotation);

        void onFailedToCaptureCameraFrame(DragonflySnapshotException e);
    }

    interface LensSnapshotInteractor extends BaseInteractorContract<LensSnapshotInteractor.SnapshotCallbacks> {

        void saveSnapshot(byte[] data, int width, int height, int rotation);

        interface SnapshotCallbacks {

            void onFailedToSaveSnapshot(DragonflySnapshotException e);

            void onSnapshotSaved(DragonflyClassificationInput snapshot);
        }
    }
}
