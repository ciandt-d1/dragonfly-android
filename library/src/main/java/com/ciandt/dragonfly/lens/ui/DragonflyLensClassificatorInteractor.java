package com.ciandt.dragonfly.lens.ui;

import com.ciandt.dragonfly.base.ui.BaseInteractorContract.AsyncTaskResult;
import com.ciandt.dragonfly.base.ui.ClassificatorInteractor;
import com.ciandt.dragonfly.data.model.Model;
import com.ciandt.dragonfly.image_processing.ImageUtils;
import com.ciandt.dragonfly.image_processing.YuvNv21ToRGBA888Converter;
import com.ciandt.dragonfly.infrastructure.ClassificationConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.infrastructure.Hashing;
import com.ciandt.dragonfly.infrastructure.system.MemoryHelper;
import com.ciandt.dragonfly.lens.data.DragonflyClassificationInput;
import com.ciandt.dragonfly.lens.exception.DragonflyClassificationException;
import com.ciandt.dragonfly.lens.exception.DragonflyModelException;
import com.ciandt.dragonfly.lens.exception.DragonflyNoMemoryAvailableException;
import com.ciandt.dragonfly.tensorflow.Classifier;
import com.ciandt.dragonfly.tensorflow.TensorFlowImageClassifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.TimingLogger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by iluz on 5/26/17.
 */

public class DragonflyLensClassificatorInteractor implements ClassificatorInteractor {

    // Manually set to avoid '"DragonflyLensClassificatorInteractor" exceeds limit of 23 characters'
    private static final String LOG_TAG = "ClassificatorInteractor";

    private final Context context;
    private final MemoryHelper memoryHelper;

    private LensClassificatorInteractorCallbacks classificationCallbacks;

    private Classifier classifier;
    private Model model;

    private LoadModelTask loadModelTask;
    private AnalyzeFromUriTask analyzeFromUriTask;
    private AsyncTask analyzeYUVN21Task;

    private ClassificationConfig classificationConfig;

    private final YuvNv21ToRGBA888Converter yuvToRgbConverter;

    private boolean isAnalyzingFromUri = false;

    private final Map<String, Map<String, Classifier.Classification>> classifications = new HashMap<>();

    private CountDownLatch modelLoadingCountDown;

    public DragonflyLensClassificatorInteractor(Context context, MemoryHelper memoryHelper) {
        this(context, null, memoryHelper);
    }

    public DragonflyLensClassificatorInteractor(Context context, ClassificationConfig classificationConfig, MemoryHelper memoryHelper) {
        this.context = context.getApplicationContext();
        this.yuvToRgbConverter = new YuvNv21ToRGBA888Converter(context);
        this.classificationConfig = classificationConfig;
        this.memoryHelper = memoryHelper;
    }

    @Override
    public void setClassificationCallbacks(LensClassificatorInteractorCallbacks classificationCallbacks) {
        this.classificationCallbacks = classificationCallbacks;
    }

    @Override
    public void setClassificationConfig(ClassificationConfig classificationConfig) {
        this.classificationConfig = classificationConfig;
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
            DragonflyLogger.debug(LOG_TAG, "LoadModelTask is not cancelled. Cancelling it.");
            loadModelTask.cancel(true);
        }

        loadModelTask = new LoadModelTask(this);
        AsyncTaskCompat.executeParallel(loadModelTask, model);
    }

    @Override
    public void releaseModel() {
        model = null;

        classifications.clear();

        if (classifier != null) {
            classifier.close();
        }
    }

    @Override
    public void analyzeFromUri(final Uri uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri can't be null.");
        }

        if (analyzeFromUriTask != null && AsyncTask.Status.RUNNING.equals(analyzeFromUriTask.getStatus())) {
            DragonflyLogger.debug(LOG_TAG, "AnalyzeFromUriTask is running. Skipping this round.");
            return;
        }

        AnalyzeFromUriTask.TaskParams taskParams = new AnalyzeFromUriTask.TaskParams(uri);
        analyzeFromUriTask = new AnalyzeFromUriTask(this);
        AsyncTaskCompat.executeParallel(analyzeFromUriTask, taskParams);
    }

    @Override
    public void analyzeYuvNv21Frame(byte[] data, int width, int height, int rotation) {
        if (!isModelLoaded()) {
            DragonflyLogger.warn(LOG_TAG, "No model loaded. Skipping analyzeYuvNv21Frame() call.");
            return;
        }

        if (data == null) {
            throw new IllegalArgumentException("data can't be null.");
        }

        if (isAnalyzingFromUri) {
            DragonflyLogger.debug(LOG_TAG, "isAnalyzingFromUri is true. Skipping this round.");
            return;
        }

        if (analyzeYUVN21Task != null && AsyncTask.Status.RUNNING.equals(analyzeYUVN21Task.getStatus())) {
            DragonflyLogger.debug(LOG_TAG, "AnalyzeYUVN21Task is running. Skipping this round.");
            return;
        }

        boolean isDecaymentAlgorithmEnabled = classificationConfig != null
                && ClassificationConfig.CLASSIFICATION_ATENUATION_ALGORITHM_DECAY.equals(classificationConfig.getClassificationAtenuationAlgorithm());

        if (isDecaymentAlgorithmEnabled) {
            analyzeYUVN21Task = new AnalyzeYUVN21WithDecaymentTask(this, classifications);
            AnalyzeYUVN21WithDecaymentTask.TaskParams taskParams = new AnalyzeYUVN21WithDecaymentTask.TaskParams(data, width, height, rotation);

            AsyncTaskCompat.executeParallel(analyzeYUVN21Task, taskParams);
        } else {
            analyzeYUVN21Task = new AnalyzeYUVN21Task(this);
            AnalyzeYUVN21Task.TaskParams taskParams = new AnalyzeYUVN21Task.TaskParams(data, width, height, rotation);

            AsyncTaskCompat.executeParallel(analyzeYUVN21Task, taskParams);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isModelLoaded() {
        return this.model != null && (modelLoadingCountDown != null && modelLoadingCountDown.getCount() == 0);
    }

    private void waitForModelToLoad() {
        try {
            if (modelLoadingCountDown != null) {
                modelLoadingCountDown.await();
            }
        } catch (InterruptedException e) {
            DragonflyLogger.error(LOG_TAG, e);
        }
    }

    private static class LoadModelTask extends AsyncTask<Model, Void, AsyncTaskResult<Model, DragonflyModelException>> {

        private final DragonflyLensClassificatorInteractor interactor;

        public LoadModelTask(DragonflyLensClassificatorInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<Model, DragonflyModelException> doInBackground(Model... models) {
            this.interactor.modelLoadingCountDown = new CountDownLatch(1);

            Model model = models[0];

            DragonflyLogger.debug(LOG_TAG, String.format("LoadModelTask.doInBackground() - start | model: %s", model));

            if (!hasEnoughMemory(model.getSizeInBytes())) {
                String exceptionmessage = String.format("No memory available for loading model %s", model.getId());
                DragonflyModelException exception = new DragonflyModelException(exceptionmessage, new DragonflyNoMemoryAvailableException(), model);

                return new AsyncTaskResult<>(null, exception);
            }

            try {
                interactor.classifier = TensorFlowImageClassifier.create(
                        interactor.context.getAssets(),
                        model.getModelPath(),
                        model.getLabelFilesPaths(),
                        model.getInputSize(),
                        model.getImageMean(),
                        model.getImageStd(),
                        model.getInputName(),
                        model.getOutputNames()
                );

                return new AsyncTaskResult<>(model, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to load model %s at %s", model.getId(), model.getModelPath());

                return new AsyncTaskResult<>(null, new DragonflyModelException(errorMessage, e, model));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Model, DragonflyModelException> result) {
            this.interactor.modelLoadingCountDown.countDown();

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

        private boolean hasEnoughMemory(long modelSizeInBytes) {
            long requiredAvailableMemory = Float.valueOf(modelSizeInBytes * DragonflyConfig.getUncompressedModelSizeCalculatorFactor()).longValue();

            return interactor.memoryHelper.hasEnoughMemory(requiredAvailableMemory, MemoryHelper.MemoryUnit.BYTES);
        }
    }

    private static class AnalyzeFromUriTask extends AsyncTask<AnalyzeFromUriTask.TaskParams, Void, AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException>> {

        private final DragonflyLensClassificatorInteractor interactor;
        private TaskParams taskParams;

        private String savedImagePath;

        public AnalyzeFromUriTask(DragonflyLensClassificatorInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException> doInBackground(TaskParams... params) {
            interactor.isAnalyzingFromUri = true;

            this.taskParams = params[0];

            DragonflyLogger.debug(LOG_TAG, "AnalyzeFromUriTask.doInBackground() - start");

            TimingLogger timings = new TimingLogger(LOG_TAG, "AnalyzeFromUriTask.analyzeFromUri()");

            if (!this.interactor.isModelLoaded()) {
                this.interactor.waitForModelToLoad();
                timings.addSplit("Model loaded.");
            }

            try {
                if (!this.interactor.isModelLoaded()) {
                    DragonflyLogger.warn(LOG_TAG, "No model loaded. Skipping analyzeFromUri() call.");

                    DragonflyModelException modelException = new DragonflyModelException("Model was not loaded.", null);
                    return new AsyncTaskResult<>(null, new DragonflyClassificationException(modelException.getMessage(), modelException));
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                InputStream inputStream = interactor.context.getContentResolver().openInputStream(taskParams.uri);
                BitmapFactory.decodeStream(inputStream, null, options);
                options.inSampleSize = ImageUtils.calculateInSampleSize(options, interactor.model.getInputSize(), interactor.model.getInputSize());
                options.inJustDecodeBounds = false;


                inputStream = interactor.context.getContentResolver().openInputStream(taskParams.uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

                String filename = Hashing.SHA1(taskParams.uri.toString());
                savedImagePath = ImageUtils.saveBitmapToStagingArea(bitmap, filename);

                Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, interactor.model.getInputSize(), interactor.model.getInputSize(), false);
                timings.addSplit("Scale bitmap");

                if (DragonflyConfig.shouldSaveSelectedExistingBitmapsInDebugMode()) {
                    DragonflyLogger.warn(LOG_TAG, "Saving bitmaps for debugging.");

                    ImageUtils.saveBitmapToStagingArea(bitmap, String.format("selected-original-%s.jpg", filename));
                    ImageUtils.saveBitmapToStagingArea(croppedBitmap, String.format("selected-cropped-%s.jpg", filename));
                }

                Map<String, List<Classifier.Classification>> results = interactor.classifier.classifyImage(croppedBitmap);
                timings.addSplit("Classify image");

                return new AsyncTaskResult<>(results, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to analyze bitmap with error: %s", e.getMessage());

                return new AsyncTaskResult<>(null, new DragonflyClassificationException(errorMessage, e));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException> result) {
            interactor.isAnalyzingFromUri = false;

            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeFromUriTask.onPostExecute() - error | exception: %s", result.getError()));

                interactor.classificationCallbacks.onUriAnalysisFailed(taskParams.uri, result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeFromUriTask.onPostExecute() - success | classifications: %s", result.getResult()));

                DragonflyClassificationInput classificationInput = DragonflyClassificationInput.newBuilder().withImagePath(savedImagePath).build();
                interactor.classificationCallbacks.onUriAnalyzed(taskParams.uri, classificationInput, result.getResult());
            }
        }

        public static class TaskParams {

            private final Uri uri;

            public TaskParams(Uri uri) {
                this.uri = uri;
            }

            public Uri getUri() {
                return uri;
            }
        }
    }

    private static class AnalyzeYUVN21Task extends AsyncTask<AnalyzeYUVN21Task.TaskParams, Void, AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException>> {

        private final DragonflyLensClassificatorInteractor interactor;

        public AnalyzeYUVN21Task(DragonflyLensClassificatorInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException> doInBackground(TaskParams... params) {
            TaskParams taskParams = params[0];

            DragonflyLogger.debug(LOG_TAG, "AnalyzeYUVN21Task.doInBackground() - start");

            // To see the log ouput, make sure to run the command below:
            // adb shell setprop log.tag.<LOG_TAG> VERBOSE
            TimingLogger timings = new TimingLogger(LOG_TAG, "AnalyzeYUVN21Task.doInBackground()");

            try {
                Bitmap bitmap = interactor.yuvToRgbConverter.convert(taskParams.getData(), taskParams.getWidth(), taskParams.getHeight(), Bitmap.Config.ARGB_8888, taskParams.getRotation());
                timings.addSplit("Convert YUV to RGB");

                Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, interactor.model.getInputSize(), interactor.model.getInputSize(), false);
                timings.addSplit("Scale bitmap");

                Map<String, List<Classifier.Classification>> results = interactor.classifier.classifyImage(croppedBitmap);
                timings.addSplit("Classify image");

                timings.dumpToLog();

                if (DragonflyConfig.shouldSaveCapturedCameraFramesInDebugMode()) {
                    DragonflyLogger.warn(LOG_TAG, "Saving bitmaps for debugging.");

                    String baseName = String.format("%s-%s", System.currentTimeMillis(), UUID.randomUUID().toString());
                    ImageUtils.saveBitmapToStagingArea(bitmap, String.format("captured-original-%s.png", baseName));
                    ImageUtils.saveBitmapToStagingArea(croppedBitmap, String.format("captured-cropped-%s.png", baseName));
                }

                return new AsyncTaskResult<>(results, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to analyze byte array with error: %s", e.getMessage());

                timings.addSplit(e.getMessage());
                timings.dumpToLog();

                return new AsyncTaskResult<>(null, new DragonflyClassificationException(errorMessage, e));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException> result) {
            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21Task.onPostExecute() - error | exception: %s", result.getError()));

                interactor.classificationCallbacks.onYuvNv21AnalysisFailed(result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21Task.onPostExecute() - success | recognitions: %s", result.getResult()));

                interactor.classificationCallbacks.onYuvNv21Analyzed(result.getResult());
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


    private static class AnalyzeYUVN21WithDecaymentTask extends AsyncTask<AnalyzeYUVN21WithDecaymentTask.TaskParams, Void, AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException>> {

        private final DragonflyLensClassificatorInteractor interactor;
        private final Map<String, Map<String, Classifier.Classification>> classifications;

        private final float DECAY_VALUE;
        private final float UPDATE_VALUE;
        private final float MINIMUM_THRESHOLD;

        public AnalyzeYUVN21WithDecaymentTask(DragonflyLensClassificatorInteractor interactor, Map<String, Map<String, Classifier.Classification>> classifications) {
            this.interactor = interactor;
            this.classifications = classifications;

            DECAY_VALUE = interactor.classificationConfig.getClassificationAtenuationDecayDecayValue();
            UPDATE_VALUE = interactor.classificationConfig.getClassificationAtenuationDecayUpdateValue();
            MINIMUM_THRESHOLD = interactor.classificationConfig.getClassificationAtenuationDecayMinimumThreshold();
        }

        @Override
        protected AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException> doInBackground(TaskParams... params) {
            TaskParams taskParams = params[0];

            DragonflyLogger.debug(LOG_TAG, "AnalyzeYUVN21WithDecaymentTask.doInBackground() - start");

            // To see the log ouput, make sure to run the command below:
            // adb shell setprop log.tag.<LOG_TAG> VERBOSE
            TimingLogger timings = new TimingLogger(LOG_TAG, "AnalyzeYUVN21WithDecaymentTask.doInBackground()");

            try {
                Bitmap bitmap = interactor.yuvToRgbConverter.convert(taskParams.getData(), taskParams.getWidth(), taskParams.getHeight(), Bitmap.Config.ARGB_8888, taskParams.getRotation());
                timings.addSplit("Convert YUV to RGB");

                Bitmap croppedBitmap = Bitmap.createScaledBitmap(bitmap, interactor.model.getInputSize(), interactor.model.getInputSize(), false);
                timings.addSplit("Scale bitmap");

                Map<String, List<Classifier.Classification>> results = interactor.classifier.classifyImage(croppedBitmap);
                timings.addSplit("Classify image");

                results.putAll(optimizeClassifications(results));
                timings.addSplit("Optimize classifications");

                timings.dumpToLog();

                if (DragonflyConfig.shouldSaveCapturedCameraFramesInDebugMode()) {
                    DragonflyLogger.warn(LOG_TAG, "Saving bitmaps for debugging.");

                    String baseName = String.format("%s-%s", System.currentTimeMillis(), UUID.randomUUID().toString());
                    ImageUtils.saveBitmapToStagingArea(bitmap, String.format("captured-original-%s.png", baseName));
                    ImageUtils.saveBitmapToStagingArea(croppedBitmap, String.format("captured-cropped-%s.png", baseName));
                }

                return new AsyncTaskResult<>(results, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to analyze byte array with error: %s", e.getMessage());

                timings.addSplit(e.getMessage());
                timings.dumpToLog();

                return new AsyncTaskResult<>(null, new DragonflyClassificationException(errorMessage, e));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<Map<String, List<Classifier.Classification>>, DragonflyClassificationException> result) {
            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21WithDecaymentTask.onPostExecute() - error | exception: %s", result.getError()));

                interactor.classificationCallbacks.onYuvNv21AnalysisFailed(result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("AnalyzeYUVN21WithDecaymentTask.onPostExecute() - success | recognitions: %s", result.getResult()));

                interactor.classificationCallbacks.onYuvNv21Analyzed(result.getResult());
            }
        }

        private Map<String, List<Classifier.Classification>> optimizeClassifications(Map<String, List<Classifier.Classification>> newClassifications) {

            Map<String, List<Classifier.Classification>> results = new HashMap<>();

            for (Map.Entry<String, List<Classifier.Classification>> entry : newClassifications.entrySet()) {

                String category = entry.getKey();

                Map<String, Classifier.Classification> decayedClassifications = new HashMap<>();

                Map<String, Classifier.Classification> categoryClassifications = classifications.get(category);
                if (categoryClassifications == null) {
                    categoryClassifications = new HashMap<>();
                }

                for (String key : categoryClassifications.keySet()) {

                    Classifier.Classification oldClassification = categoryClassifications.get(key);

                    float decayedConfidence = oldClassification.getConfidence() * DECAY_VALUE;
                    if (decayedConfidence > MINIMUM_THRESHOLD) {
                        decayedClassifications.put(key, oldClassification.clone(decayedConfidence));
                    }
                }

                categoryClassifications.clear();
                categoryClassifications.putAll(decayedClassifications);

                for (Classifier.Classification newClassification : entry.getValue()) {

                    float oldConfidence = 0.0f;
                    Classifier.Classification oldClassification = categoryClassifications.get(newClassification.getId());
                    if (oldClassification != null) {
                        oldConfidence = oldClassification.getConfidence();
                    }

                    float updatedConfidence = oldConfidence + (newClassification.getConfidence() * UPDATE_VALUE);
                    categoryClassifications.put(newClassification.getId(), newClassification.clone(updatedConfidence));
                }

                this.classifications.put(category, categoryClassifications);

                List<Classifier.Classification> categoryResults = new ArrayList<>(categoryClassifications.values());
                Collections.sort(categoryResults, new Comparator<Classifier.Classification>() {

                    @Override
                    public int compare(Classifier.Classification c1, Classifier.Classification c2) {
                        return Float.compare(c2.getConfidence(), c1.getConfidence());
                    }
                });

                results.put(category, categoryResults);
            }

            return results;
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
