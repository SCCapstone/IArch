package com.github.IArch;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class AsyncDrawable extends BitmapDrawable {
	private final WeakReference<GalleryWorkerTask> galleryWorkerTaskReference;

    public AsyncDrawable(Resources res, Bitmap bitmap,
            GalleryWorkerTask galleryWorkerTask) {
        super(res, bitmap);
        galleryWorkerTaskReference =
            new WeakReference<GalleryWorkerTask>(galleryWorkerTask);
    }

    public GalleryWorkerTask getGalleryWorkerTask() {
        return galleryWorkerTaskReference.get();
    }

}
