package com.ciandt.dragonfly.example.config

import android.Manifest

/**
 * Created by iluz on 7/10/17.
 */
interface PermissionsMapping {
    companion object {
        val SAVE_IMAGE_TO_GALLERY = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val REAL_TIME: String = Manifest.permission.CAMERA
    }
}