package com.github.IArch;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageDetailsFragment extends Fragment implements OnClickListener {

	static String fileLocation;
	View galleryView;
	ImageView image;
	String fileName;
	
	public ImageDetailsFragment(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		galleryView = inflater.inflate(R.layout.fragment_image_details, container, false);
		getActionBar().setTitle(R.string.title_fragment_image_details);
		image = (ImageView) galleryView.findViewById(R.id.imageViewID);	
		image.setOnClickListener(this);
		
		fileLocation = fileName;
		dropboxStuff();
		
		//show actionbar in case it was hidden when displaying full screen image
		getActivity().getActionBar().show();
		
		return galleryView;
	}

	private ActionBar getActionBar() {
	    return getActivity().getActionBar();
	}
	
	private void setPic(String file) {
		//get dimensions of view
		ImageView myImage = (ImageView) galleryView.findViewById(R.id.imageViewID);
		
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
		
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
	
	void dropboxStuff() {
		if (MainActivity.mAccountManager.hasLinkedAccount()) {	
			//show picture that was taken
			setPic(fileLocation);
			String[] splitFile = fileLocation.split("/");
			
			try {
				//open datastore and get fresh data
				DbxDatastore datastore = MainActivity.mDatastoreManager.openDatastore(splitFile[6]);
				datastore.sync();
				
				//open table
				DbxTable tasksTbl = datastore.getTable("Picture_Data");
				
				//query table for results
				DbxFields queryParams = new DbxFields().set("LOCAL_FILENAME", fileLocation);
				DbxTable.QueryResult results = tasksTbl.query(queryParams);
				if (results.hasResults()) {
					DbxRecord firstResult = results.iterator().next();
				
					//get data for variables
					String date = firstResult.getString("DATE");
					String projectName = firstResult.getString("PROJECT_NAME");
					String description = firstResult.getString("DESCRIPTION");
					Double longitude = firstResult.getDouble("LONGITUDE");
					Double latitude = firstResult.getDouble("LATITUDE");
					String latStr = "Latitude: " + latitude;
					String longStr = "Longitude: " + longitude;
					String latLong = "Latitude: " + latitude + " Longitude: " + longitude;
					String artifactType = firstResult.getString("ARTIFACT_TYPE");
					String location = firstResult.getString("LOCATION");
				
					//set text for textViews
					if (date != null) {
						TextView dateField = (TextView) galleryView.findViewById(R.id.date);
						dateField.setText(date);
					}
					if (projectName != null) {
						TextView nameField = (TextView) galleryView.findViewById(R.id.project_name);
						nameField.setText("Project: " + projectName);
					}
					if (description != null) {
						TextView descriptionField = (TextView) galleryView.findViewById(R.id.description);
						descriptionField.setText("Description: " + description);
					}
					if (latitude != null) {
						TextView latField = (TextView) galleryView.findViewById(R.id.latitude);
						latField.setText(latStr);
					}
					if (longitude != null) {
						TextView longField = (TextView) galleryView.findViewById(R.id.longitude);
						longField.setText(longStr);
					}
					if (artifactType != null) {
						TextView artifactField = (TextView) galleryView.findViewById(R.id.artifact_name);
						artifactField.setText("Artifact: " + artifactType);
					}
					if (location != null) {
						TextView locationField = (TextView) galleryView.findViewById(R.id.location_name);
						locationField.setText("Location: " + location);
					}
				
					//close datastores
					datastore.close();
				} else {
					//picture clicked had no data attached to it, do something here
					datastore.close();
				}
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			//show picture that was taken
			setPic(fileLocation);
		}		
	}

	@Override
	public void onClick(View v) {
		// Create new fragment and transaction
				Fragment newFragment = new FullscreenImageFragment();
				FragmentTransaction transaction = getFragmentManager().beginTransaction();

				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack
				transaction.replace(R.id.container, newFragment);
				transaction.addToBackStack(null);

				// Commit the transaction
				transaction.commit();
		
	}
}
