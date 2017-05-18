package com.ciandt.dragonfly;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.util.List;

public class Dragonfly {

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";

    public static int getVersion() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static List<Recognition> classify(AssetManager assetManager, String model, String label, Bitmap bitmap) throws Exception {

        TensorFlowImageClassifier classifier = TensorFlowImageClassifier.create(
                assetManager,
                model,
                label,
                INPUT_SIZE,
                IMAGE_MEAN,
                IMAGE_STD,
                INPUT_NAME,
                OUTPUT_NAME);

        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        final List<Recognition> results = classifier.recognizeImage(bitmap);

        classifier.close();

        return results;
    }
}
