package com.ciandt.dragonfly.infrastructure;

import com.ciandt.dragonfly.BuildConfig;

/**
 * Created by iluz on 5/31/17.
 */

public class DragonflyConfig {

    private static boolean saveBitmapsInDebugMode = false;

    public static boolean shouldSaveBitmapsInDebugMode() {
        return saveBitmapsInDebugMode;
    }

    public static void shouldSaveBitmapsInDebugMode(boolean saveBitmapsInDebugMode) {
        DragonflyConfig.saveBitmapsInDebugMode = BuildConfig.DEBUG && saveBitmapsInDebugMode;
    }
}
