package com.github.IArch;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
	static double latitude;
	static double longitude;
	static LocationManager locationManager;
	static LocationListener locationListener;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		getLocation();
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
			
			TextView myText = (TextView) findViewById(R.id.textView1);
			myText.setText("Latitude: " + latitude + " " + "Longitude: " + longitude);
			//System.out.println("MADE IT HERE");
						
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
					
			//get link from dropbox and create remote path for sync; create datastore
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(MainActivity.mAccountManager.getLinkedAccount());
			DbxFile testFile = dbxFs.create(new DbxPath(splitFile[6]));
			
			try {
			    //create remote file and assign it to photo
				File fileVar = new File(fileLocation);
			    testFile.writeFromExistingFile(fileVar, false);
			  
			    //set up dropbox datastores
			    DbxDatastore datastore = MainActivity.mDatastoreManager.openDefaultDatastore();
				DbxTable tasksTbl = datastore.getTable("tasks");
				DbxRecord task = tasksTbl.insert().set("filename", fileLocation).set("latitude", latitude).set("longitude", longitude);
				
				//sync datastore
				datastore.sync();
				
				//close datastore
				datastore.close();
				
				//stop looking for location updates; saves battery
				locationManager.removeUpdates(locationListener);
				
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
		
		/*
		File imgFile = new File(fileLocation);
		Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		ImageView myImage2 = (ImageView) findViewById(R.id.imageView1);
		myImage2.setImageBitmap(myBitmap);
		*/
	}
	
	void getLocation() {
		//acquire a reference to system location manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		//define listener that responds to location updates
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				// called when new location is found by network location provider
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
		};
		String locationProvider = LocationManager.GPS_PROVIDER;
		//cached last known location
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
	}
		
}
