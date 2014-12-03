package com.github.IArch;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.GridView;

public class Gallery extends Activity {

	private GridView gridView;
	private GridViewAdapter customGridAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		gridView = (GridView) findViewById(R.id.gridView);
		customGridAdapter = new GridViewAdapter(this, R.layout.row_grid, getData());
		gridView.setAdapter(customGridAdapter);
	}

	private ArrayList getData() {
		final ArrayList imageItems = new ArrayList();
		/*
		// retrieve String drawable array
		TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
		for (int i = 0; i < imgs.length(); i++) {
			Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
					imgs.getResourceId(i, -1));
			imageItems.add(new ImageItem(bitmap, "Image#" + i));
		}
		*/
		File path = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + "/iArch/");
	    File[] imageFiles = path.listFiles();
	    
	    for (int i = 0; i < imageFiles.length; i++) {
	        Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[i].getAbsolutePath());
	        Bitmap smallBitmap = bitmap.createScaledBitmap(bitmap, 200, 200, false);
	        imageItems.add(new ImageItem(smallBitmap, "Image#" + i));
	    }
	    /*
	    Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[1].getAbsolutePath());
	    Bitmap smallBitmap = bitmap.createScaledBitmap(bitmap, 200, 200, false);
        imageItems.add(new ImageItem(smallBitmap, "Image#" + 1));
        
        bitmap = BitmapFactory.decodeFile(imageFiles[2].getAbsolutePath());
        imageItems.add(new ImageItem(bitmap, "Image#" + 2));
*/
		return imageItems;

	}
}
