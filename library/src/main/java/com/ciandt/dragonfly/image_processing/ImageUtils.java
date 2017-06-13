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
import android.graphics.BitmapFactory;
import android.util.Log;

import com.ciandt.dragonfly.infrastructure.DragonflyConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    public static boolean saveBitmap(final Bitmap bitmap, final String filename) throws IOException {
        final String root = DragonflyConfig.getDropboxPath();
        if (root == null) {
            throw new IllegalStateException("DragonflyConfig.setDropboxPath() should be called with a writable system path");
        }

        boolean wasSuccessful;

        Log.i(LOG_TAG, String.format("Saving %dx%d bitmap to %s.", bitmap.getWidth(), bitmap.getHeight(), root));
        final File myDir = new File(root);

        if (!myDir.exists() && !myDir.mkdirs()) {
            throw new IllegalStateException("Failed to create destination folder.");
        }

        final File file = new File(myDir, filename);
        if (file.exists()) {
            file.delete();
        }

        final FileOutputStream out = new FileOutputStream(file);
        try {
            bitmap.compress(Bitmap.CompressFormat.WEBP, 99, out);
            out.flush();

            wasSuccessful = true;
        } catch (final Exception e) {
            throw new IOException(e);
        } finally {
            if (out != null) {
                out.close();
            }
        }


        return wasSuccessful;
    }

    public static Bitmap loadBitmapFromDisk(String path) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        } else {
            return null;
        }
    }
}
