package com.github.IArch;

import java.lang.ref.WeakReference;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class GalleryWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
    Bitmap image_path;
    int reqWidth;
    int reqHeight;

    public GalleryWorkerTask(ImageView imageView) {
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

	@Override
	protected Bitmap doInBackground(Integer... params) {
		//int data = params[0];
		//Bitmap picture = BitmapFactory.decodeFile(image_path);
        int width = image_path.getWidth();
        int height = image_path.getHeight();
        float aspectRatio = (float) width / (float) height;
        int newWidth = 200;
        int newHeight = (int) (200 / aspectRatio);
        return image_path = Bitmap.createScaledBitmap(image_path, newWidth,
                newHeight, true);
	}
	
	@Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        this.cancel(true);
    }

}
