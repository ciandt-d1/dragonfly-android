package com.ciandt.dragonfly.infrastructure;

import android.Manifest;

import java.util.Collections;
import java.util.List;

/**
 * Created by iluz on 6/12/17.
 */

public interface PermissionsMapping {

    List<String> CAPTURE_FRAME = Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    List<String> SAVE_IMAGE_TO_GALLERY = Collections.singletonList(Manifest.permission.WRITE_EXTERNAL_STORAGE);
}
