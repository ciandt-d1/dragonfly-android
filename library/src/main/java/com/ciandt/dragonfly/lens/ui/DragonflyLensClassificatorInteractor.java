package com.ciandt.dragonfly.lens.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.TimingLogger;

import com.ciandt.dragonfly.base.ui.BaseInteractorContract.AsyncTaskResult;
import com.ciandt.dragonfly.base.ui.ClassificatorInteractor;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.image_processing.ImageUtils;
import com.ciandt.dragonfly.image_processing.YUVNV21ToRGBA888Converter;
import com.ciandt.dragonfly.infrastructure.DragonflyConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;
import com.ciandt.dragonfly.tensorflow.TensorFlowImageClassifier;

import java.util.List;
import java.util.UUID;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyLensClassificatorInteractor implements ClassificatorInteractor {

    // Manually set to avoid '"DragonflyLensClassificatorInteractor" exceeds limit of 23 characters'
    private static final String LOG_TAG = "ClassificatorInteractor";

    private final Context context;

    private LensClassificatorInteractorCallbacks classificationCallbacks;

    private Classifier classifier;
    private Model model;

    private LoadModelTask loadModelTask;
    private AnalyzeBitmapTask analyzeBitmapTask;
    private AnalyzeYUVN21Task analyzeYUVN21Task;

    private final YUVNV21ToRGBA888Converter yuvToRgbConverter;

    public DragonflyLensClassificatorInteractor(Context context) {
        this.context = context.getApplicationContext();
        this.yuvToRgbConverter = new YUVNV21ToRGBA888Converter(context);
    }

    @Override
    public void setClassificationCallbacks(LensClassificatorInteractorCallbacks classificationCallbacks) {
        this.classificationCallbacks = classificationCallbacks;
    }

    @Override
    public void loadModel(Model model) {
        if (model == null) {
            throw new IllegalArgumentException("model can't be null.");
        }

        if (model.equals(this.model)) {
            DragonflyLogger.info(LOG_TAG, "This model is already currently setup. Ignoring it.");
            return;
        }

        if (loadModelTask != null && !loadModelTask.isCancelled()) {
            DragonflyLogger.debug(LOG_TAG, "LoadModelTask is not cancelled. Cancelling it..");
            loadModelTask.cancel(true);
        }

        loadModelTask = new LoadModelTask(this);
        AsyncTaskCompat.executeParallel(loadModelTask, model);
    }

    @Override
    public void releaseModel() {
        model = null;

        if (classifier != null) {
            classifier.close();
        }
    }

    @Override
    public void analyzeBitmap(final Bitmap bitmap) {
        if (!isModelLoaded()) {
            DragonflyLogger.warn(LOG_TAG, "No model loaded. Skipping analyzeBitmap() call.");
            return;
        }

        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap can't be null.");
        }

        if (analyzeBitmapTask != null && AsyncTask.Status.RUNNING.equals(analyzeBitmapTask.getStatus())) {
            DragonflyLogger.debug(LOG_TAG, "AnalyzeBitmapTask is running. Skipping this round.");
            return;
        }

        analyzeBitmapTask = new AnalyzeBitmapTask(this);
        AsyncTaskCompat.executeParallel(analyzeBitmapTask, bitmap);
    }

    @Override
    public void analyzeYUVNV21Picture(byte[] data, int width, int height, int rotation) {
        if (!isModelLoaded()) {
            DragonflyLogger.warn(LOG_TAG, "No model loaded. Skipping analyzeYUVNV21Picture() call.");
            return;
        }

        if (data == null) {
            throw new IllegalArgumentException("data can't be null.");
        }

        if (analyzeYUVN21Task != null && AsyncTask.Status.RUNNING.equals(analyzeYUVN21Task.getStatus())) {
            DragonflyLogger.debug(LOG_TAG, "AnalyzeYUVN21Task is running. Skipping this round.");
            return;
        }

        analyzeYUVN21Task = new AnalyzeYUVN21Task(this);

        AnalyzeYUVN21Task.TaskParams taskParams = new AnalyzeYUVN21Task.TaskParams(data, width, height, rotation);
        AsyncTaskCompat.executeParallel(analyzeYUVN21Task, taskParams);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isModelLoaded() {
        return model != null;
    }

    private static class LoadModelTask extends AsyncTask<Model, Void, AsyncTaskResult<Model, DragonflyModelException>> {

        private final DragonflyLensClassificatorInteractor interactor;

        public LoadModelTask(DragonflyLensClassificatorInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<Model, DragonflyModelException> doInBackground(Model... models) {
            Model model = models[0];

            DragonflyLogger.debug(LOG_TAG, String.format("LoadModelTask.doInBackground() - start | model: %s", model));

            try {
                interactor.classifier =
                        TensorFlowImageClassifier.create(
                                interactor.context.getAssets(),
                                model.getModelPath(),
                                model.getLabelsPath(),
                                model.getInputSize(),
                                model.getImageMean(),
                                model.getImageStd(),
                                model.getInputName(),
                                model.getOutputName());

                return new AsyncTaskResult<>(model, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to load model %s at %s", model.getName(), model.getModelPath());

                return new AsyncTaskResult<>(null, new DragonflyModelException(errorMessage, e, model));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Model, DragonflyModelException> result) {
            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("LoadModelTask.onPostExecute() - error | exception: %s", result.getError()));

                interactor.classificationCallbacks.onModelFailure(result.getError());
            } else {
                Model model = result.getResult();

                DragonflyLogger.debug(LOG_TAG, String.format("LoadModelTask.onPostExecute() - success | model: %s", result.getResult()));

                interactor.model = model;
                interactor.classificationCallbacks.onModelReady(model);
            }
        }
    }

    private static class AnalyzeBitmapTask extends AsyncTask<Bitmap, Void, AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException>> {

        private final DragonflyLensClassificatorInteractor interactor;

        public AnalyzeBitmapTask(DragonflyLensClassificatorInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException> doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];

            DragonflyLogger.debug(LOG_TAG, "AnalyzeBitmapTask.doInBackground() - start");

            // To see the log ouput, make sure to run the command below:
            // adb shell setprop log.tag.<LOG_TAG> VERBOSE
            TimingLogger timings = new TimingLogger(LOG_TAG, "AnalyzeBitmapTask.doInBackground()");

            try {
                Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, interactor.model.getInputSize(), interactor.model.getInputSize(), false);
                timings.addSplit("Scale bitmap");

                List<Classifier.Recognition> results = interactor.classifier.recognizeImage(croppedBitmap);
                timings.addSplit("Classify image");

                return new AsyncTaskResult<>(results, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to analyze bitmap with error: %s", e.getMessage());

                return new AsyncTaskResult<>(null, new DragonflyRecognitionException(errorMessage, e));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException> result) {
            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeBitmapTask.onPostExecute() - error | exception: %s", result.getError()));

                interactor.classificationCallbacks.onImageAnalysisFailed(result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeBitmapTask.onPostExecute() - success | recognitions: %s", result.getResult()));

                interactor.classificationCallbacks.onImageAnalyzed(result.getResult());
            }
        }
    }

    private static class AnalyzeYUVN21Task extends AsyncTask<AnalyzeYUVN21Task.TaskParams, Void, AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException>> {

        private final DragonflyLensClassificatorInteractor interactor;

        public AnalyzeYUVN21Task(DragonflyLensClassificatorInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException> doInBackground(AnalyzeYUVN21Task.TaskParams... params) {
            AnalyzeYUVN21Task.TaskParams taskParams = params[0];

            DragonflyLogger.debug(LOG_TAG, "AnalyzeYUVN21Task.doInBackground() - start");

            // To see the log ouput, make sure to run the command below:
            // adb shell setprop log.tag.<LOG_TAG> VERBOSE
            TimingLogger timings = new TimingLogger(LOG_TAG, "AnalyzeYUVN21Task.doInBackground()");

            try {
                Bitmap bitmap = interactor.yuvToRgbConverter.convert(taskParams.getData(), taskParams.getWidth(), taskParams.getHeight(), Bitmap.Config.ARGB_8888, taskParams.getRotation());
                timings.addSplit("Convert YUV to RGB");

                Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, interactor.model.getInputSize(), interactor.model.getInputSize(), false);
                timings.addSplit("Scale bitmap");

                List<Classifier.Recognition> results = interactor.classifier.recognizeImage(croppedBitmap);
                timings.addSplit("Classify image");

                timings.dumpToLog();

                if (DragonflyConfig.shouldSaveBitmapsInDebugMode()) {
                    DragonflyLogger.warn(LOG_TAG, "Saving bitmaps for debugging.");

                    ImageUtils.saveBitmap(bitmap, String.format("original-%s%s.png", System.currentTimeMillis(), UUID.randomUUID().toString()));
                    ImageUtils.saveBitmap(croppedBitmap, String.format("cropped-%s%s.png", System.currentTimeMillis(), UUID.randomUUID().toString()));
                }

                return new AsyncTaskResult<>(results, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to analyze byte array with error: %s", e.getMessage());

                timings.addSplit(e.getMessage());
                timings.dumpToLog();

                return new AsyncTaskResult<>(null, new DragonflyRecognitionException(errorMessage, e));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException> result) {
            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21Task.onPostExecute() - error | exception: %s", result.getError()));

                interactor.classificationCallbacks.onImageAnalysisFailed(result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21Task.onPostExecute() - success | recognitions: %s", result.getResult()));

                interactor.classificationCallbacks.onImageAnalyzed(result.getResult());
            }
        }

        public static class TaskParams {

            private final byte[] data;
            private final int width;
            private final int height;
            private final int rotation;

            public TaskParams(byte[] data, int width, int height, int rotation) {
                this.data = data;
                this.width = width;
                this.height = height;
                this.rotation = rotation;
            }

            public byte[] getData() {
                return data;
            }

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }

            public int getRotation() {
                return rotation;
            }
        }
    }

}
