package com.ciandt.dragonfly.lens.ui;

import com.ciandt.dragonfly.base.ui.BaseInteractorContract;
import com.ciandt.dragonfly.base.ui.BasePresenterContract;
import com.ciandt.dragonfly.base.ui.BaseViewContract;
import com.ciandt.dragonfly.base.ui.Orientation;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.infrastructure.ClassificationConfig;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyClassificationException;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException;
import com.ciandt.dragonfly.tensorflow.Classifier;

import android.net.Uri;
import android.support.v4.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * Created by iluz on 5/22/17.
 */

public interface DragonflyLensRealTimeContract {

    interface LensRealTimeView extends BaseViewContract {

        void start();

        void stop();

        void loadModel(Model model);

        void unloadModel();

        Map<String, List<Classifier.Classification>> getLastClassifications();

        void setLastClassifications(Map<String, List<Classifier.Classification>> classifications);

        void setClassificationConfig(ClassificationConfig classificationConfig);

        void setLabels(List<Pair<String, Integer>> labels);

        void setOrientation(@Orientation.Mode int orientation);

        void onStartLoadingModel(Model model);

        void onModelReady(Model model);

        void onModelLoadFailure(DragonflyModelException e);

        void onUriAnalyzed(Uri uri, DragonflyClassificationInput classificationInput, Map<String, List<Classifier.Classification>> classifications);

        void onUriAnalysisFailed(Uri uri, DragonflyClassificationException e);

        void onYuvNv21AnalysisFailed(DragonflyClassificationException e);

        void captureCameraFrame();

        void onStartTakingSnapshot();

        void onSnapshotTaken(DragonflyClassificationInput snapshot);

        void onSnapshotError(DragonflySnapshotException e);
    }

    interface LensRealTimePresenter extends BasePresenterContract<LensRealTimeView> {

        void setClassificationConfig(ClassificationConfig classificationConfig);

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
