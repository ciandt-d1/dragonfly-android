package com.ciandt.dragonfly.image_processing;

import android.graphics.Bitmap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by iluz on 5/27/17.
 */

public class BitmapManager {

    private Map<String, CacheEntry> bitmapCache;
    private int cacheCapacity = 5;

    public BitmapManager() {
        // Intentionally empty.
    }

    @SuppressWarnings("unused")
    public BitmapManager(int cacheCapacity) {
        this.cacheCapacity = cacheCapacity;
    }

    public Bitmap get(int width, int height, Bitmap.Config config) {
        String key = buildCacheKey(width, height, config);

        if (bitmapCache == null) {
            bitmapCache = new HashMap<>(cacheCapacity);
        }

        if (bitmapCache.containsKey(key)) {
            CacheEntry cacheEntry = bitmapCache.get(key);
            cacheEntry.usageCount++;

            return cacheEntry.bitmap;
        }

        if (bitmapCache.size() == cacheCapacity) {
            purgeLeastUsedFromCache();
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        CacheEntry cacheEntry = new CacheEntry();
        cacheEntry.usageCount++;
        cacheEntry.bitmap = bitmap;
        bitmapCache.put(key, cacheEntry);

        return cacheEntry.bitmap;
    }

    private void purgeLeastUsedFromCache() {
        if (bitmapCache == null) {
            return;
        }

        String leastUsedKey = null;
        CacheEntry leastUsedValue = null;
        for (Map.Entry<String, CacheEntry> entry : bitmapCache.entrySet()) {
            if (leastUsedKey == null) {
                leastUsedKey = entry.getKey();
                leastUsedValue = entry.getValue();
            }

            if (entry.getValue().usageCount < leastUsedValue.usageCount) {
                leastUsedKey = entry.getKey();
                leastUsedValue = entry.getValue();
            }
        }

        if (leastUsedKey != null) {
            bitmapCache.remove(leastUsedKey);
        }
    }

    private String buildCacheKey(int width, int height, Bitmap.Config config) {
        return String.format("%s-%s-%s", width, height, config);
    }

    private static class CacheEntry {

        int usageCount = 0;
        Bitmap bitmap = null;
    }
}
