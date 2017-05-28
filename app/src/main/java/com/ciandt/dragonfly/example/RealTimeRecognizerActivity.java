package com.ciandt.dragonfly.example;

import android.Manifest;
import android.os.Bundle;

import com.ciandt.dragonfly.data.Model;
import com.ciandt.dragonfly.example.shared.BaseActivity;
import com.ciandt.dragonfly.lens.ui.DragonflyLensView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class RealTimeRecognizerActivity extends BaseActivity {

    private final static String LOG_TAG = RealTimeRecognizerActivity.class.getSimpleName();

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String MODEL_FILE = "file:///android_asset/model1.pb";
    private static final String LABEL_FILE = "file:///android_asset/model1.txt";

    private DragonflyLensView dragonflyLensView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_recognizer);

        dragonflyLensView = (DragonflyLensView) findViewById(R.id.dragonFlyLens);
        dragonflyLensView.setModel(model());
    }

    @Override
    protected void onResume() {
        super.onResume();

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (!report.areAllPermissionsGranted()) {
                    MultiplePermissionsListener dialogMultiplePermissionsListener =
                            DialogOnAnyDeniedMultiplePermissionsListener.Builder
                                    .withContext(RealTimeRecognizerActivity.this)
                                    .withTitle("Permissions required")
                                    .withMessage("Both camera and write to external storage permissions are needed.")
                                    .withButtonText(android.R.string.ok)
                                    .withIcon(R.mipmap.ic_launcher)
                                    .build();
                } else {
                    if (dragonflyLensView != null) {
                        dragonflyLensView.start();
                    }
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

            }
        }).check();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (dragonflyLensView != null) {
            dragonflyLensView.stop();
        }
    }

    private Model model() {
        Model model = new Model("fakeId")
                .setInputSize(INPUT_SIZE)
                .setImageMean(IMAGE_MEAN)
                .setImageStd(IMAGE_STD)
                .setInputName(INPUT_NAME)
                .setOutputName(OUTPUT_NAME)
                .setModelPath(MODEL_FILE)
                .setLabelsPath(LABEL_FILE);

        return model;
    }
}
