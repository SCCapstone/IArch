package com.github.IArch;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private ArrayList<ImageItem> getData() {
		final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
		
		File path = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + "/iArch/");
	    File[] imageFiles = path.listFiles();
	    
	    for (int i = 0; i < imageFiles.length; i++) {
	        Bitmap bitmap = BitmapFactory.decodeFile(imageFiles[i].getAbsolutePath());
	        Bitmap smallBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
	        imageItems.add(new ImageItem(smallBitmap, "Image#" + i));
	    }
	    
		return imageItems;

	}
}
