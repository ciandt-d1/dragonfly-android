package com.ciandt.dragonfly.example.config;

import com.ciandt.dragonfly.example.BuildConfig;

/**
 * Created by iluz on 5/15/17.
 */

public interface Features {

    boolean ENABLE_DEBUG_DRAWER = BuildConfig.DEBUG && true;
}
