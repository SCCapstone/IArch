package com.github.IArch;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class Chooser extends Activity {

	private GridView gridView;
	private GridViewAdapter customGridAdapter;
	public static File fileName = null;
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chooser);
		
		mTitle = getString(R.string.project_management);
		getActionBar().setTitle(mTitle);

		gridView = (GridView) findViewById(R.id.gridView);
		customGridAdapter = new GridViewAdapter(this, R.layout.row_grid, getData());
		gridView.setAdapter(customGridAdapter);
		
		//handle item click
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) {
				//Toast.makeText(Gallery.this, position + "#Selected",
				//		Toast.LENGTH_SHORT).show();
				
				//get files in images directory
				File path = new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES) + "/iArch/");
			    File[] imageFiles = path.listFiles();
			    //use it like imageFiles[position]
			    fileName = imageFiles[position];
			    System.out.println("image selected : " + imageFiles[position]);
				if (fileName.isDirectory()) {
					System.out.println("THIS IS A DIRECTORY");
					
					Intent intent = new Intent(Chooser.this, Gallery.class);
				    startActivity(intent);
				} else {
					Intent intent = new Intent(Chooser.this, ImageDetails.class);
				    startActivity(intent);
				}
			}

		});
	}

	private ArrayList<ImageItem> getData() {
		final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
		
		File path = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + "/iArch/");
	    File[] imageFiles = path.listFiles();
	    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.folder_icon_small);
	    
	    if (imageFiles != null) {
	    	for (int i = 0; i < imageFiles.length; i++) {
	    		String folderName = imageFiles[i].toString();
	    		String[] shortFolderName = folderName.split("/");
	    		imageItems.add(new ImageItem(icon, shortFolderName[6]));
	    		//imageItems.add(new ImageItem(decodeSampledBitmapFromFile(imageFiles[i].getAbsolutePath(), 200, 200), shortFolderName[6]));
	    	}
	    }
	    
		return imageItems;
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
    //System.out.println(inSampleSize);
    return inSampleSize;
}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}
	
}
