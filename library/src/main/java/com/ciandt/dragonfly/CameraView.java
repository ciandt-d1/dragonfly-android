package com.ciandt.dragonfly;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.ciandt.dragonfly.base.ui.Orientation;
import com.ciandt.dragonfly.base.ui.Size;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String LOG_TAG = CameraView.class.getSimpleName();

    private static final int CAMERA_ROTATION_LANDSCAPE = 0;
    private static final int CAMERA_ROTATION_PORTRAIT = 90;

    private @Orientation.Mode
    int orientation;

    private Camera camera;
    private LensViewCallback callback;

    private int frameTimeInterval = 1000;

    private Camera.Size previewSize;

    private boolean isPreviewActive = false;

    private boolean snapshoting = false;

    private int orientationDegrees;

    public CameraView(Context context) {
        super(context);

        orientationDegrees = calculateOrientationDegrees();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);

        orientationDegrees = calculateOrientationDegrees();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        orientationDegrees = calculateOrientationDegrees();
    }

    public void setOrientation(@Orientation.Mode int orientation) {
        this.orientation = orientation;
    }

    public void start() throws Exception {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - start()", LOG_TAG));

        camera = Camera.open();
        if (camera == null) {
            throw new Exception("Failed to open Camera");
        }

        camera.setDisplayOrientation(orientationDegrees);
        getHolder().addCallback(this);
    }

    public void stop() {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - stop()", LOG_TAG));

        if (camera == null) {
            DragonflyLogger.debug(LOG_TAG, String.format("%s - stop() - camera is null (already stopped)", LOG_TAG));
            return;
        }

        stopPreview();

        camera.release();
        camera = null;
        getHolder().removeCallback(this);
    }

    public void setCallback(LensViewCallback callback) {
        this.callback = callback;
    }

    public void setFrameTimeInterval(int frameTimeInterval) {
        this.frameTimeInterval = frameTimeInterval;
    }

    public void takeSnapshot() {
        snapshoting = true;
    }

    /**
     * Internal methods
     */
    private void startPreview() {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - startPreview()", LOG_TAG));

        snapshoting = false;

        if (isPreviewActive) {
            DragonflyLogger.debug(LOG_TAG, String.format("%s - startPreview() - preview is already active. Skipping", LOG_TAG));
            return;
        }

        try {
            camera.setPreviewDisplay(getHolder());
            camera.startPreview();
            isPreviewActive = true;

            capture();

            if (callback != null) {
                callback.onPreviewStarted(convertSize(previewSize), orientationDegrees);
            }
        } catch (IOException e) {
            DragonflyLogger.error(LOG_TAG, e.getMessage());
        }
    }

    private void stopPreview() {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - stopPreview()", LOG_TAG));

        if (!isPreviewActive) {
            DragonflyLogger.debug(LOG_TAG, String.format("%s - stopPreview() - Preview is already inactive. Skipping.", LOG_TAG));
            return;
        }

        try {
            isPreviewActive = false;

            camera.setPreviewCallback(null);
            camera.stopPreview();

        } catch (Exception e) {
            // This will happen when camera is not running
            DragonflyLogger.error(LOG_TAG, e);
        }
    }

    private void configPreview(int width, int height) {
        if (camera == null) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);

        // Focus
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        // Preview Size
        previewSize = getBestPreviewSize(width, height, parameters);
        DragonflyLogger.debug(LOG_TAG, String.format("getBestPreviewSize - width: %s, height: %s", previewSize.width, previewSize.height));

        parameters.setPreviewSize(previewSize.width, previewSize.height);

        camera.setParameters(parameters);
    }

    // Credits:
    // https://github.com/florent37/CameraFragment/blob/master/camerafragment/src/main/java/com/github/florent37/camerafragment/internal/utils/CameraHelper.java
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : supportedPreviewSizes) {
            double ratio = (double) size.width / size.height;

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : supportedPreviewSizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    /**
     * Surface methods
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - surfaceCreated()", LOG_TAG));
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - surfaceChanged()", LOG_TAG));

        if (holder.getSurface() == null) {
            return;
        }

        stopPreview();
        configPreview(width, height);
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - surfaceDestroyed()", LOG_TAG));
        stopPreview();
    }

    /**
     * Camera Preview Callback
     */
    private void capture() {
        DragonflyLogger.debug(LOG_TAG, String.format("%s - capture() - isPreviewActive: %s", LOG_TAG, isPreviewActive));

        if (camera != null && isPreviewActive) {
            camera.setOneShotPreviewCallback(this);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (callback == null) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();

        if (snapshoting) {
            stopPreview();
            snapshoting = false;

            callback.onSnapshotCaptured(data, convertSize(parameters.getPreviewSize()), orientationDegrees);
            return;
        }

        callback.onFrameReady(data, convertSize(parameters.getPreviewSize()), orientationDegrees);

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                capture();
            }
        }, frameTimeInterval);
    }

    private Size convertSize(Camera.Size cameraSize) {
        return new Size(cameraSize.width, cameraSize.height);
    }

    // https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation%28int%29
    private int calculateOrientationDegrees() {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = (info.orientation - degrees + 360) % 360;

        return result;
    }

    /**
     * Callback interface
     */
    public interface LensViewCallback {

        void onFrameReady(byte[] data, Size previewSize, int rotation);

        void onSnapshotCaptured(byte[] data, Size previewSize, int rotation);

        void onPreviewStarted(Size previewSize, int rotation);
    }
}
