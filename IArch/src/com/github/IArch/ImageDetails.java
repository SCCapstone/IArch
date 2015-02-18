package com.github.IArch;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageDetails extends Activity {

	String fileLocation = Gallery.fileName.toString();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_details);
		dropboxStuff();
		
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
			
			try {
				//open datastore and get fresh data
				DbxDatastore datastore = MainActivity.mDatastoreManager.openDefaultDatastore();
				datastore.sync();
				
				//open table
				DbxTable tasksTbl = datastore.getTable("tasks");
				
				//query table for results
				DbxFields queryParams = new DbxFields().set("LOCAL_FILENAME", fileLocation);
				DbxTable.QueryResult results = tasksTbl.query(queryParams);
				DbxRecord firstResult = results.iterator().next();
				
				//get data for variables
				String date = firstResult.getString("DATE");
				String projectName = firstResult.getString("PROJECT_NAME");
				String description = firstResult.getString("DESCRIPTION");
				Double longitude = firstResult.getDouble("LONGITUDE");
				Double latitude = firstResult.getDouble("LATITUDE");
				String latLong = "Latitude: " + latitude + " Longitude: " + longitude;
				String artifactType = firstResult.getString("ARTIFACT_TYPE");
				String location = firstResult.getString("LOCATION");
				
				//set text for textViews
				if (date != null) {
					TextView dateField = (TextView)findViewById(R.id.date);
					dateField.setText(date);
				}
				if (projectName != null) {
					TextView nameField = (TextView)findViewById(R.id.project_name);
					nameField.setText("Project Name : " + projectName);
				}
				if (description != null) {
					TextView descriptionField = (TextView)findViewById(R.id.description);
					descriptionField.setText("Description: " + description);
				}
				if (latLong != null) {
					TextView latLongField = (TextView)findViewById(R.id.textView1);
					latLongField.setText(latLong);
				}
				if (artifactType != null) {
					TextView artifactField = (TextView)findViewById(R.id.artifact_name);
					artifactField.setText("Artifact: " + artifactType);
				}
				if (location != null) {
					TextView locationField = (TextView)findViewById(R.id.location_name);
					locationField.setText("Location: " + location);
				}
				
				//close datastores
				datastore.close();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			//show picture that was taken
			setPic(fileLocation);
		}		
	}
}
