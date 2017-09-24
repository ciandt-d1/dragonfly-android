package com.ciandt.dragonfly.infrastructure;

import com.ciandt.dragonfly.BuildConfig;

/**
 * Created by iluz on 5/31/17.
 */

public class DragonflyConfig {

    private static boolean saveCapturedCameraFramesInDebugMode = false;

    private static boolean saveSelectedExistingBitmapsInDebugMode = false;

    private static String stagingPath;

    private static String userSavedImagesPath;

    private static int maxModelLoadingRetryAttempts;

    private static long realTimeControlsVisibilityAnimationDuration = 2500;

    // if you want this to be disabled, set it to 1.
    private static float uncompressedModelSizeCalculatorFactor = 3;

    public static boolean shouldSaveCapturedCameraFramesInDebugMode() {
        return saveCapturedCameraFramesInDebugMode;
    }

    public static void shouldSaveCapturedCameraFramesInDebugMode(boolean saveBitmapsInDebugMode) {
        DragonflyConfig.saveCapturedCameraFramesInDebugMode = BuildConfig.DEBUG && saveBitmapsInDebugMode;
    }

    public static boolean shouldSaveSelectedExistingBitmapsInDebugMode() {
        return saveSelectedExistingBitmapsInDebugMode;
    }

    public static void shouldSaveSelectedExistingBitmapsInDebugMode(boolean saveForDebugging) {
        DragonflyConfig.saveSelectedExistingBitmapsInDebugMode = saveForDebugging;
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

    public static long getRealTimeControlsVisibilityAnimationDuration() {
        return realTimeControlsVisibilityAnimationDuration;
    }

    public static void setRealTimeControlsVisibilityAnimationDuration(long realTimeControlsVisibilityAnimationDuration) {
        DragonflyConfig.realTimeControlsVisibilityAnimationDuration = realTimeControlsVisibilityAnimationDuration;
    }

    public static float getUncompressedModelSizeCalculatorFactor() {
        return uncompressedModelSizeCalculatorFactor;
    }

    public static void setUncompressedModelSizeCalculatorFactor(float uncompressedModelSizeCalculatorFactor) {
        DragonflyConfig.uncompressedModelSizeCalculatorFactor = uncompressedModelSizeCalculatorFactor;
    }
}
