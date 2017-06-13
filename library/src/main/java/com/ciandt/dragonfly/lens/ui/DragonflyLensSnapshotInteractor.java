package com.ciandt.dragonfly.lens.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.TimingLogger;

import com.ciandt.dragonfly.image_processing.ImageUtils;
import com.ciandt.dragonfly.image_processing.YUVNV21ToRGBA888Converter;
import com.ciandt.dragonfly.infrastructure.DragonflyConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;
import com.ciandt.dragonfly.lens.data.DragonflyCameraSnapshot;
import com.ciandt.dragonfly.lens.exception.DragonflySnapshotException;

import java.io.File;
import java.util.UUID;

/**
 * Created by iluz on 6/9/17.
 */

public class DragonflyLensSnapshotInteractor implements DragonflyLensRealTimeContract.LensSnapshotInteractor {

    // Manually set to avoid '"DragonflyLensSnapshotInteractor" exceeds limit of 23 characters'
    private static final String LOG_TAG = "SnapshotInteractor";

    private SaveSnapshotTask saveSnapshotTask;

    private final YUVNV21ToRGBA888Converter yuvToRgbConverter;
    private SnapshotCallbacks snapshotCallbacks;

    public DragonflyLensSnapshotInteractor(Context context) {
        this.yuvToRgbConverter = new YUVNV21ToRGBA888Converter(context);
    }

    @Override
    public void saveSnapshot(byte[] data, int width, int height, int rotation) {
        if (data == null) {
            throw new IllegalArgumentException("data can't be null.");
        }

        if (saveSnapshotTask != null && !saveSnapshotTask.isCancelled()) {
            DragonflyLogger.debug(LOG_TAG, "LoadModelTask is not cancelled. Cancelling it..");
            saveSnapshotTask.cancel(true);
        }

        saveSnapshotTask = new SaveSnapshotTask(this);

        SaveSnapshotTask.TaskParams taskParams = new SaveSnapshotTask.TaskParams(data, width, height, rotation);
        AsyncTaskCompat.executeParallel(saveSnapshotTask, taskParams);
    }

    @Override
    public void setCallbacks(SnapshotCallbacks callbacks) {
        this.snapshotCallbacks = callbacks;
    }

    private static class SaveSnapshotTask extends AsyncTask<SaveSnapshotTask.TaskParams, Void, AsyncTaskResult<DragonflyCameraSnapshot, DragonflySnapshotException>> {

        private final DragonflyLensSnapshotInteractor interactor;

        public SaveSnapshotTask(DragonflyLensSnapshotInteractor interactor) {
            this.interactor = interactor;
        }

        @Override
        protected AsyncTaskResult<DragonflyCameraSnapshot, DragonflySnapshotException> doInBackground(TaskParams... params) {
            TaskParams taskParams = params[0];

            DragonflyLogger.debug(LOG_TAG, "SaveSnapshotTask.doInBackground() - start");

            // To see the log ouput, make sure to run the command below:
            // adb shell setprop log.tag.<LOG_TAG> VERBOSE
            TimingLogger timings = new TimingLogger(LOG_TAG, "SaveSnapshotTask.doInBackground()");

            try {
                Bitmap bitmap = interactor.yuvToRgbConverter.convert(taskParams.getData(), taskParams.getWidth(), taskParams.getHeight(), Bitmap.Config.ARGB_8888, taskParams.getRotation());
                timings.addSplit("Convert YUV to RGB");

                String fileName = UUID.nameUUIDFromBytes(taskParams.data).toString();
                ImageUtils.saveBitmap(bitmap, String.format(fileName, System.currentTimeMillis(), UUID.randomUUID().toString()));
                timings.addSplit("Saved snapshot to disk.");

                timings.dumpToLog();

                String filePath = DragonflyConfig.getDropboxPath() + File.separator + fileName;
                DragonflyCameraSnapshot snapshot = DragonflyCameraSnapshot.newBuilder()
                        .withPath(filePath)
                        .withWidth(taskParams.width)
                        .withHeight(taskParams.height)
                        .build();

                return new AsyncTaskResult<>(snapshot, null);
            } catch (Exception e) {
                String errorMessage = String.format("Failed to save snapshot with error: %s", e.getMessage());

                timings.addSplit(e.getMessage());
                timings.dumpToLog();

                return new AsyncTaskResult<>(null, new DragonflySnapshotException(errorMessage, e));
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<DragonflyCameraSnapshot, DragonflySnapshotException> result) {
            if (result.hasError()) {
                DragonflyLogger.debug(LOG_TAG, String.format("SaveSnapshotTask.onPostExecute() - error | exception: %s", result.getError()));

                interactor.snapshotCallbacks.onFailedToSaveSnapshot(result.getError());
            } else {
                DragonflyLogger.debug(LOG_TAG, String.format("SaveSnapshotTask.onPostExecute() - success | snapshot: %s", result.getResult()));

                interactor.snapshotCallbacks.onSnapshotSaved(result.getResult());
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
