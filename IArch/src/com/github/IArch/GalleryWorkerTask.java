package com.github.IArch;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

public class GalleryWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
	int data = 0;
	File myImage;
    int reqWidth;
    int reqHeight;

    public GalleryWorkerTask(ImageView imageView) {
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

	@Override
	protected Bitmap doInBackground(Integer... params) {
		data = params[0];
		String path = myImage.toString();
		Bitmap image = decodeSampledBitmapFromFile(path, 256, 256);
		
		return image;
	}
	
	@Override
    protected void onPostExecute(Bitmap bitmap) {
		if (isCancelled()) {
			bitmap = null;
	    }
		
        if (imageViewReference != null && bitmap != null) {
        	final ImageView imageView = imageViewReference.get();
            final GalleryWorkerTask galleryWorkerTask =
                    getGalleryWorkerTask(imageView);
            if (this == galleryWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        this.cancel(true);
    }

    private static GalleryWorkerTask getGalleryWorkerTask(ImageView imageView) {
    	   if (imageView != null) {
    	       final Drawable drawable = imageView.getDrawable();
    	       if (drawable instanceof AsyncDrawable) {
    	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
    	           return asyncDrawable.getGalleryWorkerTask();
    	       }
    	    }
    	    return null;
    	}
    
    public static Bitmap decodeSampledBitmapFromFile(String file, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(file, options);
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
	
}
