package com.ciandt.dragonfly;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final String LOG_TAG = CameraView.class.getSimpleName();
    public static final int CAMERA_ROTATION = 90;

    private Camera camera;
    private LensViewCallback callback;

    private int frameTimeInterval = 1000;

    private Camera.Size previewSize;


    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void start() throws Exception {
        camera = Camera.open();
        if (camera == null) {
            throw new Exception("Failed to open Camera");
        }

        camera.setDisplayOrientation(CAMERA_ROTATION);
        getHolder().addCallback(this);
    }

    public void stop() {
        if (camera != null) {
            camera.release();
            camera = null;
            getHolder().removeCallback(this);
        }
    }

    public int getCameraRotation() {
        return CAMERA_ROTATION;
    }


    public void setCallback(LensViewCallback callback) {
        this.callback = callback;
    }

    public void setFrameTimeInterval(int frameTimeInterval) {
        this.frameTimeInterval = frameTimeInterval;
    }

    /**
     * Internal methods
     */
    private void startPreview() {
        try {
            camera.setPreviewDisplay(getHolder());
            camera.startPreview();
            capture();

            if (callback != null) {
                callback.onPreviewStarted(convertSize(previewSize), CAMERA_ROTATION);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private void stopPreview() {
        try {
            camera.setPreviewCallback(null);
            camera.stopPreview();
        } catch (Exception e) {
            // This will happen when camera is not running
        }
    }

    private void configPreview(int width, int height) {
        if (camera == null) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();

        // Focus
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        // Preview Size
        previewSize = getBestPreviewSize(width, height, parameters);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        Log.d(LOG_TAG, "getBestPreviewSize: " + previewSize.width);
        Log.d(LOG_TAG, "getBestPreviewSize: " + previewSize.height);

        camera.setParameters(parameters);
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Surface methods
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }

        stopPreview();
        configPreview(width, height);
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    /**
     * Camera Preview Callback
     */
    private void capture() {
        if (camera != null) {
            camera.setOneShotPreviewCallback(this);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (callback == null) {
            return;
        }

        Camera.Parameters parameters = camera.getParameters();
        Size size = new Size(parameters.getPreviewSize().width, parameters.getPreviewSize().height);

        callback.onFrameReady(data, convertSize(parameters.getPreviewSize()), parameters.getPreviewFormat());

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

    /**
     * Callback interface
     */
    public interface LensViewCallback {

        void onFrameReady(byte[] data, Size previewSize, int previewFormat);

        void onPreviewStarted(Size previewSize, int rotation);
    }
}
