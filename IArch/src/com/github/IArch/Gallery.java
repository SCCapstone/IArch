package com.github.IArch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class Gallery extends Activity {

	private GridView gridView;
	private GridViewAdapter customGridAdapter;
	public static File fileName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);

		gridView = (GridView) findViewById(R.id.gridView);
		customGridAdapter = new GridViewAdapter(this, R.layout.row_grid, getData());
		gridView.setAdapter(customGridAdapter);
		gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		gridView.setMultiChoiceModeListener(new MultiChoiceModeListener());
		
		//handle item click
		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
				int position, long id) {
				//Toast.makeText(Gallery.this, position + "#Selected",
				//		Toast.LENGTH_SHORT).show();
				
				//get files in images directory
				String longFileName = Chooser.fileName.toString();
				String[] shortFileName = longFileName.split("/");
				File path = new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES) + "/iArch/" + shortFileName[6]);
			    File[] imageFiles = path.listFiles();
			    //use it like imageFiles[position]
			    fileName = imageFiles[position];
			    System.out.println("image selected : " + imageFiles[position]);
				
			    Intent intent = new Intent(Gallery.this, ImageDetails.class);
			    startActivity(intent);
			}

		});
	}

	private ArrayList<ImageItem> getData() {
		final ArrayList<ImageItem> imageItems = new ArrayList<ImageItem>();
		
		File path = new File(Chooser.fileName.toString());
	    File[] imageFiles = path.listFiles();
	    
	    for (int i = 0; i < imageFiles.length; i++) {
	    	String folderName = imageFiles[i].toString();
	    	String[] shortFolderName = folderName.split("/");
	    	imageItems.add(new ImageItem(decodeSampledBitmapFromFile(imageFiles[i].getAbsolutePath(), 200, 200), shortFolderName[7]));
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		// The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
       
		switch (item.getItemId()) {
		case R.id.action_upload:
			Toast.makeText(Gallery.this, "This will sync eventually!", 
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.action_export:
			System.out.println("START EXPORTING");
			export();
			Toast.makeText(Gallery.this, "Data Exported!", 
					Toast.LENGTH_LONG).show();
			String longFileName = Chooser.fileName.toString();
			String[] shortFileName = longFileName.split("/");
			System.out.println(shortFileName[6]);
			System.out.println("FINISHED EXPORTING");
			//export();
			return true;
		case R.id.action_settings:
			Toast.makeText(Gallery.this, "No settings yet", 
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public boolean export(){
		
		String longFileName = Chooser.fileName.toString();
		String[] shortFileName = longFileName.split("/");
		File path = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + "/iArch/" + shortFileName[6]);
		DbxPath remotePath = new DbxPath(shortFileName[6] + "/" + shortFileName[6] + ".csv");
	    File[] imageFiles = path.listFiles();
	    String finalString = "";
	    
		try{
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(MainActivity.mAccountManager.getLinkedAccount());
			//if remote file already exists, delete it before exporting new file
			if (dbxFs.exists(remotePath)) {
				dbxFs.delete(remotePath);
			}
			DbxFile exportFile = dbxFs.create(remotePath);
			
			try {
			    //testFile.writeString("Hello Dropbox!");
				
				finalString += "Date,Project Name,Description,Longitude,Latitude,Artifact Type,Location\n";
				
				for(int i = 0; i<imageFiles.length;i++)
				{
					String[] splitFile = imageFiles[i].toString().split("/");
					
					//open datastore and get fresh data
					DbxDatastore datastore = MainActivity.mDatastoreManager.openDatastore(splitFile[6]);
					datastore.sync();
					
					//open table
					DbxTable tasksTbl = datastore.getTable("Picture_Data");
					
					//query table for results
					DbxFields queryParams = new DbxFields().set("LOCAL_FILENAME", imageFiles[i].toString());
					DbxTable.QueryResult results = tasksTbl.query(queryParams);
					
					if (results.hasResults()) {
						DbxRecord firstResult = results.iterator().next();
						
						finalString += firstResult.getString("DATE");
						finalString += ",";
						finalString += firstResult.getString("PROJECT_NAME");
						finalString += ",";
						finalString += firstResult.getString("DESCRIPTION");
						finalString += ",";
						finalString += firstResult.getDouble("LONGITUDE");
						finalString += ",";
						finalString += firstResult.getDouble("LATITUDE");
						finalString += ",";
						finalString += firstResult.getString("ARTIFACT_TYPE");
						finalString += ",";
						finalString += firstResult.getString("LOCATION");
						finalString += "\n";
						
						
					
						//close datastores
						datastore.close();
					} else {
						//picture clicked had no data attached to it, do something here
						datastore.close();
					}
					
				}
				
				exportFile.writeString(finalString);
				
			    
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
			    exportFile.close();
			}
			
		}catch (Unauthorized e) {
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
	
	public class MultiChoiceModeListener implements GridView.MultiChoiceModeListener {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.setTitle("Select Items");
            mode.setSubtitle("One item selected");
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public void onItemCheckedStateChanged(ActionMode mode, int position,
				long id, boolean checked) {
			View singleView = gridView.getChildAt(position);
			if (checked == true) {
				//item checked
				singleView.setBackgroundColor(getResources().getColor(android.R.color.background_light));
			} else {
				//item unchecked
				singleView.setBackgroundColor(Color.parseColor("#fff3f3f3"));
			}
			
			//count number of items selected; display it at top of screen
			int selectCount = gridView.getCheckedItemCount();
            switch (selectCount) {
            case 1:
                mode.setSubtitle("One item selected");
                break;
            default:
                mode.setSubtitle("" + selectCount + " items selected");
                break;
            }
		}

	}
}
