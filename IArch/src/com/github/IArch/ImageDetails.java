package com.github.IArch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class ImageDetails extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_details);
		String fileLocation = Gallery.fileName.toString();
		
		if (MainActivity.mAccountManager.hasLinkedAccount()) {	
			//sync picture with dropbox
			//dropboxStuff(fileLocation);
						
			//show picture that was taken
			setPic(fileLocation);
			
		} else {
			//show picture that was taken
			setPic(fileLocation);
			
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_details, menu);
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
	
	private void setPic(String file) {
		//get dimensions of view
		ImageView myImage = (ImageView) findViewById(R.id.imageView1);
		
		//this works for now... hard coded scale factor
		int targetW = 400;//myImage.getWidth();
		int targetH = 400;//myImage.getHeight();
		
		
		System.out.println("targetW: " + targetW + " targetH: " + targetH);
		
		
		//get dimensions of bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		System.out.println("photoW: " + photoW + " photoH: " + photoH);
				
		//determine how much to scale down the image
		int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
		
		//Decode image file into a bitmap sized to fill the view
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		
		Bitmap bitmap = BitmapFactory.decodeFile(file, bmOptions);
		myImage.setImageBitmap(bitmap);
		
	}
}
