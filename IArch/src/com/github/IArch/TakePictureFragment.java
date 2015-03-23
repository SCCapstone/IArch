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
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat") public class TakePictureFragment extends Fragment 
		implements OnClickListener {

	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	static String fileLocation = null;
	static File newFileLocation;
	static private String date;
	static private String projectName;
	static private String location;
	static private String artifact;
	static private String description;
	static double latitude;
	static double longitude;
	static LocationManager locationManager;
	static LocationListener locationListener;
	static Location lastKnownLocation;
	View view;
	Button dropboxButton;
	int RESULT_OK = -1;
	int RESULT_CANCELED = 0;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_take_picture, container, false);
		
		dropboxButton = (Button) view.findViewById(R.id.sync);	
		dropboxButton.setOnClickListener(this);
		
		if (savedInstanceState == null) {
			
			Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
			getLocation();
			getDate();
			
			//Ensure there is a camera activity to handle intent
			if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
		return view;
	}

	@Override
    public void onResume() {
		super.onResume();
        System.out.println("you just resumed it");
        
        //user rotated the screen, redraw stuff
      	//show picture that was taken
      	setPic(fileLocation);
      			
      	TextView textDate = (TextView) view.findViewById(R.id.date);
      	textDate.setText(date);
      			
      	TextView myText = (TextView) view.findViewById(R.id.textView1);
      	myText.setText("Latitude: " + latitude + " " + "Longitude: " + longitude);
    	
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.take_picture, menu);
	}

	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first
	    //delete photo if back button was pressed on TakePicture after taking photo
	    if (isRemoving()) {
	    	if (fileLocation != null) {
	    		Toast.makeText(getActivity(), "Back button pressed, deleting image", Toast.LENGTH_SHORT).show();
	    		File myFile = new File(fileLocation);
	    		myFile.delete();
	    	}
	    }
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		//stop getting location updates; saves battery
		stopLocation();
		
		System.out.println("RESULT CODE: " + resultCode);
		
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
			//show picture that was taken
			setPic(fileLocation);
				
			TextView textDate = (TextView) view.findViewById(R.id.date);
			textDate.setText(date);
				
			TextView myText = (TextView) view.findViewById(R.id.textView1);
			myText.setText("Latitude: " + latitude + " " + "Longitude: " + longitude);
			
		} else if (resultCode == RESULT_CANCELED){
			//user cancelled the image capture
			getActivity().getFragmentManager().popBackStack();
		} else {
			// image capture failed, advise user
			Toast.makeText(getActivity(), "Error Capturing Image", Toast.LENGTH_SHORT).show();
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
	
	private void capturePictureData()
	{
		EditText projectEditText = (EditText) view.findViewById(R.id.project_name);
	    projectName = projectEditText.getText().toString();
	    
	    EditText locationEditText = (EditText) view.findViewById(R.id.location_name);
	    location = locationEditText.getText().toString();
	    
	    EditText artifactEditText = (EditText) view.findViewById(R.id.artifact_name);
	    artifact = artifactEditText.getText().toString();
	    
	    EditText descriptionEditText = (EditText) view.findViewById(R.id.description);
	    description = descriptionEditText.getText().toString();	    
	}
	
	//sync to dropbox click
	public void syncToDropbox()	{
		String[] splitLoc = fileLocation.split("/");
		capturePictureData();
		
		if (projectName.isEmpty()) {
			//projectName was null, give error since it is a required field
			Toast.makeText(getActivity(), "Error: Project Name is required", Toast.LENGTH_SHORT).show();
			
		} else {
			//file to copy
			File myFile = new File(fileLocation);
			//create new project directory under iArch folder
			File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "iArch/" + projectName);
			//new file to move to
			newFileLocation = new File(mediaStorageDir.toString() + "/" + splitLoc[6]);
			if(! mediaStorageDir.exists())
			{
				if(! mediaStorageDir.mkdirs())
				{
					Log.d("iArch/" + projectName, " failed to create directory");
				}
			}
			//move file to project folder
			myFile.renameTo(newFileLocation);
			
			//sync picture with dropbox upon clicking sync button
			if (MainActivity.mAccountManager.hasLinkedAccount())
			{
				Boolean syncCorrectly = dropboxStuff(fileLocation);
				if (syncCorrectly)
				{
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					
					getLocation();
					getDate();
					
					//Ensure there is a camera activity to handle intent
					if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
				// Need to add failure message
			}
		
		}
		
		
		
		
	}
	
	private Boolean dropboxStuff(String file) {
		try {
			// Get the data entered into the textboxes
			capturePictureData();
			//shorten path
			String[] splitFile = file.split("/");
					
			//get link from dropbox and create remote path for sync; create datastore
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(MainActivity.mAccountManager.getLinkedAccount());
			DbxFile testFile;
			if (projectName != "") {
				testFile = dbxFs.create(new DbxPath(projectName + "/" + splitFile[6]));
				fileLocation = newFileLocation.toString();
			}
			else {
				testFile = dbxFs.create(new DbxPath(splitFile[6]));
			}
			try {
			    //create remote file and assign it to photo
				File fileVar = new File(fileLocation);
			    testFile.writeFromExistingFile(fileVar, false);
			    //set up dropbox datastores
			    //DbxDatastore datastore = MainActivity.mDatastoreManager.openDefaultDatastore();		    
			    DbxDatastore datastore = MainActivity.mDatastoreManager.openOrCreateDatastore(projectName);
			    DbxTable dataTbl = datastore.getTable("Picture_Data");
			    
				@SuppressWarnings("unused")
				DbxRecord task = dataTbl.insert().set("LOCAL_FILENAME", fileLocation).
						set("DATE", date).
						set("LATITUDE", latitude).set("LONGITUDE", longitude).
						set("PROJECT_NAME", projectName).
						set("LOCATION", location).
						set("ARTIFACT_TYPE", artifact).
						set("DESCRIPTION", description);
					
				//sync datastore
				datastore.sync();
				
				//close datastore
				datastore.close();
				
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				//close remote file so other things can be done
			    testFile.close();
			    //return true;
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
		
		return false;
			
	}
	
	private void setPic(String file) {
		//get dimensions of view
		ImageView myImage = (ImageView) view.findViewById(R.id.imageView1);
		
		//this works for now... hard coded scale factor
		int targetW = 400;//myImage.getWidth();
		int targetH = 400;//myImage.getHeight();
				
		System.out.println("targetW: " + targetW + " targetH: " + targetH);
		
		Bitmap myBitmap = decodeSampledBitmapFromFile(file, targetW, targetH);
		myImage.setImageBitmap(myBitmap);
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
		
	@SuppressLint("SimpleDateFormat") public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
	    System.out.println("INSAMPLE SIZE: " + inSampleSize);
	    return inSampleSize;
	}
	
	void getDate()
	{
		date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
	}
	
	void getLocation() {
		//acquire a reference to system location manager
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		String locationProvider = LocationManager.GPS_PROVIDER;
		
		//check to see if last known location exists
		if (locationManager != null) {
			//set cached last known location to current location for initial state
			lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
			if (lastKnownLocation != null) {
				latitude = lastKnownLocation.getLatitude();
				longitude = lastKnownLocation.getLongitude();
			}
		}
		
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
		locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
	}
	
	void stopLocation() {
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		
		//stop looking for location updates; saves battery
		locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onClick(View v) {
		//dropbox button click event
		syncToDropbox();
	}
}