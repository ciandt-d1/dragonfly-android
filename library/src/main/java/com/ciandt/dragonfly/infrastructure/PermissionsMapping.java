package com.ciandt.dragonfly.infrastructure;

import android.Manifest;

import java.util.Arrays;
import java.util.List;

/**
 * Created by iluz on 6/12/17.
 */

public interface PermissionsMapping {

    List<String> CAPTURE_FRAME = Arrays.asList(Manifest.permission.WRITE_EXTERNAL_STORAGE);
}
