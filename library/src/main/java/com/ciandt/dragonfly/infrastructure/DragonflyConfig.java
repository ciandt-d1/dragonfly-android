package com.ciandt.dragonfly.infrastructure;

import com.ciandt.dragonfly.BuildConfig;

/**
 * Created by iluz on 5/31/17.
 */

public class DragonflyConfig {

    private static boolean saveBitmapsInDebugMode = false;

    private static String stagingPath;

    private static String userSavedImagesPath;

    private static int maxModelLoadingRetryAttempts;

    public static boolean shouldSaveBitmapsInDebugMode() {
        return saveBitmapsInDebugMode;
    }

    public static void shouldSaveBitmapsInDebugMode(boolean saveBitmapsInDebugMode) {
        DragonflyConfig.saveBitmapsInDebugMode = BuildConfig.DEBUG && saveBitmapsInDebugMode;
    }

    public static String getStagingPath() {
        return stagingPath;
    }

    public static void setStagingPath(String stagingPath) {
        DragonflyConfig.stagingPath = stagingPath;
    }

    public static String getUserSavedImagesPath() {
        return userSavedImagesPath;
    }

    public static void setUserSavedImagesPath(String userSavedImagesPath) {
        DragonflyConfig.userSavedImagesPath = userSavedImagesPath;
    }

    public static int getMaxModelLoadingRetryAttempts() {
        return maxModelLoadingRetryAttempts;
    }

    public static void setMaxModelLoadingRetryAttempts(int maxModelLoadingRetryAttempts) {
        DragonflyConfig.maxModelLoadingRetryAttempts = maxModelLoadingRetryAttempts;
    }
}
