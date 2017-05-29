package com.ciandt.dragonfly.lens.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;

import com.ciandt.dragonfly.data.Model;
import com.ciandt.dragonfly.helpers.ImageUtils;
import com.ciandt.dragonfly.helpers.YUVNV21ToRGBA888Converter;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyRecognitionException;
import com.ciandt.dragonfly.tensorflow.Classifier;
import com.ciandt.dragonfly.tensorflow.TensorFlowImageClassifier;

import java.util.List;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyLensInteractor implements DragonflyLensContract.LensInteractorContract {

    private static final String LOG_TAG = DragonflyLensInteractor.class.getSimpleName();

    private Context context;

    private DragonflyLensContract.LensPresenter presenter;

    private Classifier classifier;
    private Model model;

    private LoadModelTask loadModelTask;
    private AnalyzeBitmapTask analyzeBitmapTask;
    private AnalyzeYUVN21Task analyzeYUVN21Task;

    private YUVNV21ToRGBA888Converter yuvToRgbConverter;

    public DragonflyLensInteractor(Context context) {
        this.context = context.getApplicationContext();
        this.yuvToRgbConverter = new YUVNV21ToRGBA888Converter(context);
    }

    @Override
    public void setPresenter(DragonflyLensContract.LensPresenter presenter) {
        this.presenter = presenter;
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

    private boolean isModelLoaded() {
        return model != null;
    }

    private static class LoadModelTask extends AsyncTask<Model, Void, AsyncTaskResult<Model, DragonflyModelException>> {

        private DragonflyLensInteractor interactor;

        public LoadModelTask(DragonflyLensInteractor interactor) {
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

                interactor.presenter.onModelFailure(result.getError());
            } else {
                Model model = result.getResult();

                DragonflyLogger.debug(LOG_TAG, String.format("LoadModelTask.onPostExecute() - success | model: %s", result.getResult()));

                interactor.model = model;
                interactor.presenter.onModelReady(model);
            }
        }
    }

    private static class AnalyzeBitmapTask extends AsyncTask<Bitmap, Void, AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException>> {

        private DragonflyLensInteractor interactor;

        public AnalyzeBitmapTask(DragonflyLensInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException> doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];

            DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeBitmapTask.doInBackground() - start"));

            try {
                List<Classifier.Recognition> results = interactor.classifier.recognizeImage(bitmap);

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

                interactor.presenter.onImageAnalysisFailed(result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeBitmapTask.onPostExecute() - success | recognitions: %s", result.getResult()));

                interactor.presenter.onImageAnalyzed(result.getResult());
            }
        }
    }

    private static class AnalyzeYUVN21Task extends AsyncTask<AnalyzeYUVN21Task.TaskParams, Void, AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException>> {

        private DragonflyLensInteractor interactor;

        public AnalyzeYUVN21Task(DragonflyLensInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException> doInBackground(AnalyzeYUVN21Task.TaskParams... params) {
            AnalyzeYUVN21Task.TaskParams taskParams = params[0];

            DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21Task.doInBackground() - start"));

            try {
                Bitmap bitmap = interactor.yuvToRgbConverter.convert(taskParams.getData(), taskParams.getWidth(), taskParams.getHeight(), Bitmap.Config.ARGB_8888, taskParams.getRotation());
                Bitmap croppedBitmap = Bitmap.createBitmap(interactor.model.getInputSize(), interactor.model.getInputSize(), Bitmap.Config.ARGB_8888);

                Matrix frameToCropTransform = ImageUtils.getTransformationMatrix(
                        taskParams.getWidth(),
                        taskParams.getHeight(),
                        interactor.model.getInputSize(),
                        interactor.model.getInputSize(),
                        0,
                        true
                );

                final Canvas canvas = new Canvas(croppedBitmap);
                canvas.drawBitmap(bitmap, frameToCropTransform, null);


                DragonflyLogger.debug(LOG_TAG, "Saving bitmaps to disk.");
                ImageUtils.saveBitmap(bitmap, "original.png");
                ImageUtils.saveBitmap(croppedBitmap, "cropped.png");


                List<Classifier.Recognition> results = interactor.classifier.recognizeImage(croppedBitmap);

                return new AsyncTaskResult<>(results, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to analyze byte array with error: %s", e.getMessage());

                return new AsyncTaskResult<>(null, new DragonflyRecognitionException(errorMessage, e));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Classifier.Recognition>, DragonflyRecognitionException> result) {
            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21Task.onPostExecute() - error | exception: %s", result.getError()));

                interactor.presenter.onImageAnalysisFailed(result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21Task.onPostExecute() - success | recognitions: %s", result.getResult()));

                interactor.presenter.onImageAnalyzed(result.getResult());
            }
        }

        public static class TaskParams {

            private byte[] data;
            private int width;
            private int height;
            private int rotation;

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
