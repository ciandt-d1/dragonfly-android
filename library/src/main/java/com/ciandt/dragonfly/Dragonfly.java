package com.ciandt.dragonfly;

public class Dragonfly {

    @SuppressWarnings("SameReturnValue")
    public static int getVersion() {
        return BuildConfig.VERSION_CODE;
    }

    @SuppressWarnings("SameReturnValue")
    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }
}
