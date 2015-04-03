package com.github.IArch;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class AsyncDrawable extends BitmapDrawable {
	private final WeakReference<GalleryWorkerTask> bitmapWorkerTaskReference;

    public AsyncDrawable(Resources res, Bitmap bitmap,
            GalleryWorkerTask bitmapWorkerTask) {
        super(res, bitmap);
        bitmapWorkerTaskReference =
            new WeakReference<GalleryWorkerTask>(bitmapWorkerTask);
    }

    public GalleryWorkerTask getBitmapWorkerTask() {
        return bitmapWorkerTaskReference.get();
    }

}
