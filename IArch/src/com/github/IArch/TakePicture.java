package com.github.IArch;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class TakePicture extends Activity {

	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	static String fileLocation = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		//Ensure there is a camera activity to handle intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			//create file where photo should go
			fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
			
			//continue only if file was successfully created
			if (fileUri != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				startActivityForResult(takePictureIntent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		}
		
		//store file path to variable
		fileLocation = fileUri.getPath(); 
		
				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.take_picture, menu);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
			System.out.println("You just took a picture");
			//sync picture with dropbox
			dropboxStuff(fileLocation);
			
			//show picture that was taken
			setPic(fileLocation);
			
			//get lat & long from exif data
			try {
				//float[] latLng = null;
				ExifInterface exifInterface = new ExifInterface(fileLocation);
				String picLat = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
				String picLong = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
				
				//exifInterface.getLatLong(latLng);
				TextView myText = (TextView) findViewById(R.id.textView1);
				myText.setText("Latitude: " + picLat + " " + "Longitude: " + picLong);
				//System.out.println("MADE IT HERE");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			
			
		}
	}
	
	private static Uri getOutputMediaFileUri(int type)
	{
		return Uri.fromFile(getOutputMediaFile(type));
		
	}
	
	private static File getOutputMediaFile(int type)
	{
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "iArch");
		
		if(! mediaStorageDir.exists())
		{
			if(! mediaStorageDir.mkdirs())
			{
				Log.d("iArch", "failed to create directory");
				return null;
			}
		}
		
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if(type == MEDIA_TYPE_IMAGE)
		{
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + 
					"IMG_" + timeStamp + ".jpg");
		}
		else
		{
			return null;
		}
		
		return mediaFile;
	}
	
	static void dropboxStuff(String file) {
		try {
			//shorten path
			String[] splitFile = file.split("/");
					
			//get link from dropbox and create remote path for sync
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(MainActivity.mAccountManager.getLinkedAccount());
			DbxFile testFile = dbxFs.create(new DbxPath(splitFile[6]));
		
			try {
			    //create remote file and assign it to photo
				File fileVar = new File(fileLocation);
			    testFile.writeFromExistingFile(fileVar, false);
			    
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				//close remote file so other things can be done
			    testFile.close();
			}
			} catch (Unauthorized e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidPathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	private void setPic(String file) {
		
		//get dimensions of view
		ImageView myImage = (ImageView) findViewById(R.id.imageView1);
		
		//this doesnt work for the size but I want it to...
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
		
		/*
		File imgFile = new File(fileLocation);
		Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		ImageView myImage2 = (ImageView) findViewById(R.id.imageView1);
		myImage2.setImageBitmap(myBitmap);
		*/
	}
}
