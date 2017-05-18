package com.ciandt.dragonfly.example.config;

import com.ciandt.dragonfly.example.BuildConfig;

/**
 * Created by iluz on 5/15/17.
 */

public interface CommonBundleNames {

    String BUNDLE_NAME_TEMPLATE = BuildConfig.APPLICATION_ID + ".%s";

    String SHOW_DEBUG_DRAWER_BUNDLE = String.format(BUNDLE_NAME_TEMPLATE, "SHOW_DEBUG_DRAWER_BUNDLE");

}
