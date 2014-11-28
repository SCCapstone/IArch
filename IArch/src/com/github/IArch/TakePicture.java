package com.github.IArch;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

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
			
			//ImageView resultImage = (ImageView) findViewById(R.id.imageView1);
			//Bundle extras = data.getExtras();
			//Bitmap imageBitmap = (Bitmap) extras.get("data");
			//resultImage.setImageBitmap(imageBitmap);
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
			String[] shortFile = file.split("/");
					
			DbxFileSystem dbxFs = DbxFileSystem.forAccount(MainActivity.mAccountManager.getLinkedAccount());
			DbxFile testFile = dbxFs.create(new DbxPath(shortFile[6]));
		
			try {
			    System.out.println(fileLocation);
			    File fileVar = new File(fileLocation);
			    testFile.writeFromExistingFile(fileVar, false);
			    
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
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
	
}
