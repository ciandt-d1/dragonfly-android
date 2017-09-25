package com.ciandt.dragonfly.infrastructure.system;

import android.app.ActivityManager;
import android.content.Context;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by iluz on 9/19/17.
 */
public class MemoryHelper {

    private Context context;

    public MemoryHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    public enum MemoryUnit {
        BYTES(1L),
        KBYTES(1024 * 8),
        MBYTES(1024 * 1024 * 8),
        GBYTES(1024 * 1024 * 1024 * 8);

        private final long bytesQuantity;

        MemoryUnit(long bytesQuantity) {
            this.bytesQuantity = bytesQuantity;
        }
    }

    public Long getAvailableMemory(MemoryUnit memoryUnit) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        long availableMemory = memoryInfo.availMem / memoryUnit.bytesQuantity;

        return availableMemory;
    }

    public boolean hasEnoughMemory(long requiredMemory, MemoryUnit unit) {
        long availableMemoryInBytes = getAvailableMemory(unit);

        return availableMemoryInBytes >= requiredMemory;
    }
}