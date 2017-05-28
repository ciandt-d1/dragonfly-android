package com.ciandt.dragonfly.example;

import android.os.Bundle;
import android.util.Size;
import android.widget.TextView;

import com.ciandt.dragonfly.CameraView;
import com.ciandt.dragonfly.example.shared.BaseActivity;

public class RealTimeActivity extends BaseActivity implements CameraView.LensViewCallback {

    private CameraView cameraView;
    private TextView logView;

    private int frame = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time);

        cameraView = (CameraView) findViewById(R.id.cameraView);
        logView = (TextView) findViewById(R.id.logView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            cameraView.start();
            cameraView.setCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraView.setCallback(null);
        cameraView.stop();
    }

    @Override
    public void onFrameReady(byte[] data, Size size) {
        frame++;
        logView.setText(String.format("frame: %s | size: %s", frame, size));
    }

    @Override
    public void onPreviewStarted(Size previewSize, int rotation) {

    }
}
