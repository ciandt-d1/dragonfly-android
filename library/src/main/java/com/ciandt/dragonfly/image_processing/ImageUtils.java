/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.ciandt.dragonfly.image_processing;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Utility class for manipulating images.
 **/
public class ImageUtils {

    private static final String LOG_TAG = ImageUtils.class.getSimpleName();

    /**
     * Saves a Bitmap object to disk for analysis.
     *
     * @param bitmap   The bitmap to save.
     * @param filename The location to save the bitmap to.
     */
    public static void saveBitmap(final Bitmap bitmap, final String filename) {
        final String root =
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "tensorflow";
        Log.i(LOG_TAG, String.format("Saving %dx%d bitmap to %s.", bitmap.getWidth(), bitmap.getHeight(), root));
        final File myDir = new File(root);

        if (!myDir.exists() && !myDir.mkdirs()) {
            Log.i(LOG_TAG, "Make dir failed");
        }

        final File file = new File(myDir, filename);
        if (file.exists()) {
            file.delete();
        }
        try {
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 99, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }
}
