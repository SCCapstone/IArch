package com.github.IArch;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import android.app.ActionBar;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat") public class TakePictureFragment extends Fragment 
		implements OnClickListener, OnItemSelectedListener {

	public static final int MEDIA_TYPE_IMAGE = 1;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	static String fileLocation = null;
	static File newFileLocation;
	static private String date;
	static private String projectName;
	static private String projectTitle; // same as projectName but it won't be modified
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
	Boolean fileSynced = false;
	Spinner pSpinner;
	static List<String> list;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_take_picture, container, false);
		getActionBar().setTitle(R.string.title_fragment_take_picture);
		
		//Set up spinners
		Spinner afct = (Spinner) view.findViewById(R.id.artifact_name);
		afct.setOnItemSelectedListener(this);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
	    		R.array.artifacts, R.layout.spinner_layout); 
	    adapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
	    afct.setAdapter(adapter);
		
	    getProjectsForSpinner();
	    pSpinner = (Spinner) view.findViewById(R.id.project_name);
		ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout_map, list);
	    //ArrayAdapter<CharSequence> pAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.artifacts, R.layout.spinner_layout);
	    pAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
	    pSpinner.setPrompt("Select your Project");
	    pSpinner.setAdapter(
	    		new NothingSelectedSpinnerAdapter(
	    				pAdapter,
	    				R.layout.project_spinner_row_nothing_selected,
	    				// R.layout.project_spinner_row_nothing_selected_dropdown, //Optional
	    				getActivity()));
	    
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
					//store file path to variable
					fileLocation = fileUri.getPath();
					
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
					startActivityForResult(takePictureIntent,CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
				}
			}
		}
						
		return view;
	}

	private ActionBar getActionBar() {
	    return getActivity().getActionBar();
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
      	
      	TextView latText = (TextView) view.findViewById(R.id.latitude);
      	latText.setText("Latitude: " + latitude);
      	
      	TextView longText = (TextView) view.findViewById(R.id.longitude);
      	longText.setText("Longitude: " + longitude);
    }
	
	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first
	    //delete photo if back button was pressed on TakePicture after taking photo
	    if (isRemoving() && fileSynced == false) {
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
				
	      	TextView latText = (TextView) view.findViewById(R.id.latitude);
	      	latText.setText("Latitude: " + latitude);
	      	
	      	TextView longText = (TextView) view.findViewById(R.id.longitude);
	      	longText.setText("Longitude: " + longitude);
			
		} else if (resultCode == RESULT_CANCELED){
			//user cancelled the image capture
			fileSynced = false;
			getActivity().getFragmentManager().popBackStack();
		} else {
			// image capture failed, advise user
			Toast.makeText(getActivity(), "Error Capturing Image", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
	{
		
	}
	
	public void onNothingSelected(AdapterView<?> parent)
	{
		
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
			System.out.println("TRYING TO FIND THE RANDOM CRASH");
			return null;
		}
		
		return mediaFile;
	}
	
	private void capturePictureData()
	{
		//prevent crash when nothing is selected on spinner
		if (pSpinner.getSelectedItem() != null) {
			projectName = pSpinner.getSelectedItem().toString();
			projectTitle = projectName; // So we can add unedited name to datastore
			//convert projectName to something dropbox will accept as a datastore name
			projectName = projectName.toLowerCase(Locale.US);
			projectName = projectName.replace(" ", "_");
		} 
		System.out.println("PROJECT NAME: " + projectName);
	
	    EditText locationEditText = (EditText) view.findViewById(R.id.location_name);
	    location = locationEditText.getText().toString();
	    
	    //EditText artifactEditText = (EditText) view.findViewById(R.id.artifact_name);
	    //artifact = artifactEditText.getText().toString();
	    
	    Spinner afct = (Spinner) view.findViewById(R.id.artifact_name);
	    artifact = afct.getSelectedItem().toString();
	    
	    EditText descriptionEditText = (EditText) view.findViewById(R.id.description);
	    description = descriptionEditText.getText().toString();
		
	}
	
	//sync to dropbox click
	public void syncToDropbox()	{
		String[] splitLoc = fileLocation.split("/");
		capturePictureData();
		
		if (pSpinner.getSelectedItem() == null) {
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
					fileSynced = true;
					getActivity().getFragmentManager().popBackStack();
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
			    
			    if (datastore.getTitle() == null) // check if a title already exists (aka datastore already exists)
			    {
			    	datastore.setTitle(projectTitle); // set the datastore title
			    }
			    
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
		int targetW = 900;//myImage.getWidth();
		int targetH = 600;//myImage.getHeight();
				
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
		if (locationManager != null) {
			//stop looking for location updates; saves battery
			locationManager.removeUpdates(locationListener);
		}
	}

	@Override
	public void onClick(View v) {
		//dropbox button click event
		syncToDropbox();
	}
	
	static void getProjectsForSpinner() {
		list = new ArrayList<String>();
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "iArch");
		File[] projectList = mediaStorageDir.listFiles();
		for (int i=0; i<projectList.length; i++) {
			System.out.println("LIST: " + projectList[i].toString());
			if (projectList[i].isDirectory()){
				String[] splitList = projectList[i].toString().split("/");
				list.add(splitList[6]);
			}
		}
	}
	
}
