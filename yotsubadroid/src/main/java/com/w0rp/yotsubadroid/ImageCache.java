package com.w0rp.yotsubadroid;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * This class implements an in-memory cache to be used for loading images
 * in the board catalog.
 */
class ImageCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
    public ImageCache(int maxSize) {
        super(maxSize);
    }

    public ImageCache() {
        super(defaultMaxSize());
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return value.getAllocationByteCount();
        }

        return value.getByteCount();
    }

    public static int defaultMaxSize() {
        return (int) (Runtime.getRuntime().maxMemory() / 4);
    }
}
