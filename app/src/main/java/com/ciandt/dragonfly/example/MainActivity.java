package com.ciandt.dragonfly.example;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciandt.dragonfly.Dragonfly;
import com.ciandt.dragonfly.Recognition;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {

    private TextView info;
    private ImageView image;
    private TextView results;

    private Button btnUseAssetsModel;
    private Button btnUseCopiedModel;

    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StringBuilder information = new StringBuilder();
        information
                .append(getString(R.string.app_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
                .append("\n")
                .append(getString(R.string.library_version, Dragonfly.getVersionName(), Dragonfly.getVersion()));

        info = (TextView) findViewById(R.id.info);
        info.setText(information);

        image = (ImageView) findViewById(R.id.image);
        image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mug));

        results = (TextView) findViewById(R.id.results);

        btnUseAssetsModel = (Button) findViewById(R.id.inferFromAssetsModel);
        btnUseAssetsModel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                testTensorFlow(true);
            }
        });

        btnUseCopiedModel = (Button) findViewById(R.id.inferFromCopiedModel);
        btnUseCopiedModel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                testTensorFlow(false);
            }
        });


    }

    private void testTensorFlow(final boolean fromAssets) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        executor.execute(new Runnable() {

                            @Override
                            public void run() {
                                try {

                                    Log.d(MainActivity.class.getSimpleName(), "running...");

                                    String model;
                                    String label;
                                    if (fromAssets) {
                                        model = "file:///android_asset/model1.pb";
                                        label = "file:///android_asset/model1.txt";
                                    } else {
                                        model = getFilesDir() + "/model1.pb";
                                        label = getFilesDir() + "/model1.txt";
                                    }

                                    Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.mug);

                                    Log.d(MainActivity.class.getSimpleName(), "classifying...");

                                    final List<Recognition> classify = Dragonfly.classify(getAssets(), model, label, bitmap);

                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            results.setText(classify.toString());
                                        }
                                    });


                                } catch (final Exception e) {
                                    throw new RuntimeException("Error on TensorFlow!", e);
                                }
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }
}
