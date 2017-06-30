package com.ciandt.dragonfly.lens.ui;

import android.graphics.Bitmap;

import com.ciandt.dragonfly.base.ui.BaseInteractorContract;
import com.ciandt.dragonfly.base.ui.BasePresenterContract;
import com.ciandt.dragonfly.base.ui.BaseViewContract;
import com.ciandt.dragonfly.base.ui.Orientation;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot;
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

        void showLoading();

        void hideLoading();

        List<Classifier.Recognition> getLastClassifications();

        void setLastClassifications(List<Classifier.Recognition> classifications);

        void setLabel(String label);

        void setLabel(String label, int confidence);

        void setOrientation(@Orientation.Mode int orientation);

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void onBitmapAnalysisFailed(DragonflyRecognitionException e);

        void captureCameraFrame();

        void onStartTakingSnapshot();

        void onSnapshotTaken(DragonflyCameraSnapshot snapshot);

        void onSnapshotError(DragonflySnapshotException e);
    }

    interface LensRealTimePresenter extends BasePresenterContract<LensRealTimeView> {

        void loadModel(Model model);

        void unloadModel();

        void analyzeBitmap(Bitmap bitmap);

        void analyzeYUVNV21(byte[] data, int width, int height, int rotation);

        void onImageAnalyzed(List<Classifier.Recognition> results);

        void onImageAnalysisFailed(DragonflyRecognitionException e);

        void onModelReady(Model model);

        void onModelFailure(DragonflyModelException e);

        void takeSnapshot();

        void onSnapshotCaptured(byte[] data, int width, int height, int rotation);

        void onFailedToCaptureCameraFrame(DragonflySnapshotException e);
    }

    interface LensSnapshotInteractor extends BaseInteractorContract<LensSnapshotInteractor.SnapshotCallbacks> {

        void saveSnapshot(byte[] data, int width, int height, int rotation);

        interface SnapshotCallbacks {

            void onFailedToSaveSnapshot(DragonflySnapshotException e);

            void onSnapshotSaved(DragonflyCameraSnapshot snapshot);
        }
    }
}
