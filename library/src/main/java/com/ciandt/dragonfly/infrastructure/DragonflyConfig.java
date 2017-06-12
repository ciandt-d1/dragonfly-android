package com.ciandt.dragonfly.infrastructure;

import com.ciandt.dragonfly.BuildConfig;

/**
 * Created by iluz on 5/31/17.
 */

public class DragonflyConfig {

    private static boolean saveBitmapsInDebugMode = false;

    private static String dropboxPath;

    private static int maxModelLoadingRetryAttempts;

    public static boolean shouldSaveBitmapsInDebugMode() {
        return saveBitmapsInDebugMode;
    }

    public static void shouldSaveBitmapsInDebugMode(boolean saveBitmapsInDebugMode) {
        DragonflyConfig.saveBitmapsInDebugMode = BuildConfig.DEBUG && saveBitmapsInDebugMode;
    }

    public static String getDropboxPath() {
        return dropboxPath;
    }

    public static void setDropboxPath(String dropboxPath) {
        DragonflyConfig.dropboxPath = dropboxPath;
    }

    public static int getMaxModelLoadingRetryAttempts() {
        return maxModelLoadingRetryAttempts;
    }

    public static void setMaxModelLoadingRetryAttempts(int maxModelLoadingRetryAttempts) {
        DragonflyConfig.maxModelLoadingRetryAttempts = maxModelLoadingRetryAttempts;
    }
}
