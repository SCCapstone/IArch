package com.github.IArch;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends ArrayAdapter<ImageItem> {
	private Context context;
	private int layoutResourceId;
	private ArrayList<ImageItem> data = new ArrayList<ImageItem>();
	ViewHolder holder;
	ImageItem item;
	
	public GridViewAdapter(Context context, int layoutResourceId,
			ArrayList<ImageItem> data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			holder = new ViewHolder();
			holder.imageTitle = (TextView) row.findViewById(R.id.text);
			holder.image = (ImageView) row.findViewById(R.id.image);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		item = data.get(position);
		holder.imageTitle.setText(item.getTitle());
		//holder.image.setImageBitmap(item.getImage());
		loadBitmap(position, holder.image);
		
		return row;
	}

	static class ViewHolder {
		TextView imageTitle;
		ImageView image;
	}
	
	public void loadBitmap(int resId, ImageView imageView) {
		if (cancelPotentialWork(resId, imageView)) {
			/*
			final String imageKey = String.valueOf(resId);
			
			final Bitmap bitmap = getBitmapFromMemCache(imageKey);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				imageView.setImageResource(R.drawable.m_placeholder_icon);
		        GalleryWorkerTask task = new GalleryWorkerTask(imageView);
		        task.myImage = item.getImage();
		        task.execute(resId);
			}
			*/
			GalleryWorkerTask task = new GalleryWorkerTask(imageView);
			task.myImage = item.getImage();
			Bitmap mPlaceHolderBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.m_placeholder_icon);
			AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), mPlaceHolderBitmap, task);
			imageView.setImageDrawable(asyncDrawable);
			task.execute(resId);
		}
	}
		
	public static boolean cancelPotentialWork(int data, ImageView imageView) {
	    final GalleryWorkerTask galleryWorkerTask = getGalleryWorkerTask(imageView);

	    if (galleryWorkerTask != null) {
	        final int galleryData = galleryWorkerTask.data;
	        // If bitmapData is not yet set or it differs from the new data
	        if (galleryData == 0 || galleryData != data) {
	            // Cancel previous task
	            galleryWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
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
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        GalleryFragment.mMemoryCache.put(key, bitmap);
	    }
	}

	public Bitmap getBitmapFromMemCache(String key) {
	    return GalleryFragment.mMemoryCache.get(key);
	}
	
}
