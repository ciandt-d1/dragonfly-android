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

import com.ciandt.dragonfly.infrastructure.DragonflyConfig;
import com.ciandt.dragonfly.infrastructure.DragonflyLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Utility class for manipulating images.
 **/
public class ImageUtils {

    private static final String LOG_TAG = ImageUtils.class.getSimpleName();

    /**
     * Saves a Bitmap object to disk for analysis.
     *
     * @param bitmap   The bitmap to save.
     * @param fileName The location to save the bitmap to.
     */
    public static String saveBitmapToStagingArea(final Bitmap bitmap, final String fileName) throws IOException {
        final String stagingPath = DragonflyConfig.getStagingPath();
        if (stagingPath == null) {
            throw new IllegalStateException("DragonflyConfig.setStagingPath() should be called with a writable system path");
        }

        DragonflyLogger.info(LOG_TAG, String.format("Saving %dx%d bitmap to %s.", bitmap.getWidth(), bitmap.getHeight(), stagingPath));
        final File stagingDir = new File(stagingPath);

        if (!stagingDir.exists() && !stagingDir.mkdirs()) {
            throw new IllegalStateException("Failed to create staging folder.");
        }

        File nomediaFile = new File(stagingDir, ".nomedia");
        if (!nomediaFile.exists()) {
            nomediaFile.createNewFile();
        }

        final File file = new File(stagingDir, fileName);
        if (file.exists()) {
            file.delete();
        }

        final FileOutputStream out = new FileOutputStream(file);
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
        } catch (final Exception e) {
            throw new IOException(e);
        } finally {
            out.close();
        }

        return file.getAbsolutePath();
    }

    public static String saveBitmapToGallery(final String fileName) throws IOException {
        final String stagingPath = DragonflyConfig.getStagingPath();
        if (stagingPath == null) {
            throw new IllegalStateException("DragonflyConfig.setStagingPath() should be called with a writable system path");
        }

        final String userSavedImagesPath = DragonflyConfig.getUserSavedImagesPath();
        if (userSavedImagesPath == null) {
            throw new IllegalStateException("DragonflyConfig.setUserSavedImagesPath() should be called with a writable system path");
        }

        final File userSavedImagesDir = new File(userSavedImagesPath);
        if (!userSavedImagesDir.exists() && !userSavedImagesDir.mkdirs()) {
            throw new IllegalStateException("Failed to create gallery-visible folder.");
        }

        String src = stagingPath + File.separator + fileName;
        String dst = DragonflyConfig.getUserSavedImagesPath() + File.separator + fileName;

        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try {
            inStream = new FileInputStream(src);
            outStream = new FileOutputStream(dst);

            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inStream != null) {
                inStream.close();
            }

            if (outStream != null) {
                outStream.close();
            }
        }

        return dst;
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
