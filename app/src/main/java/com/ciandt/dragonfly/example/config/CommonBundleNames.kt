package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig

/**
 * Created by iluz on 5/15/17.
 */

interface CommonBundleNames {
    companion object {

        val BUNDLE_NAME_TEMPLATE = BuildConfig.APPLICATION_ID + ".%s"

        val SHOW_DEBUG_DRAWER_BUNDLE = String.format(BUNDLE_NAME_TEMPLATE, "SHOW_DEBUG_DRAWER_BUNDLE")

        val PROJECT_CHANGED = String.format(BUNDLE_NAME_TEMPLATE, "PROJECT_CHANGED")
    }
}
