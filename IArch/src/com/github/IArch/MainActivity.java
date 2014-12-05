package com.github.IArch;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;

public class MainActivity extends Activity {

	static final int REQUEST_LINK_TO_DBX = 0;
	
	private Button mLinkButton;
	static DbxAccountManager mAccountManager;
	static DbxDatastoreManager mDatastoreManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), "fapxgsf7glvwkb0", "1swwbsarfhraqab");

	    // Button to link to Dropbox
	    mLinkButton = (Button) findViewById(R.id.link_button);
	    mLinkButton.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            mAccountManager.startLink((Activity)MainActivity.this, REQUEST_LINK_TO_DBX);
	        }
	    });

	    // Set up the datastore manager
	    if (mAccountManager.hasLinkedAccount()) {
	        try {
	            // Use Dropbox datastores
	            mDatastoreManager = DbxDatastoreManager.forAccount(mAccountManager.getLinkedAccount());
	            mLinkButton.setText("Unlink from Dropbox");
	        } catch (DbxException.Unauthorized e) {
	            System.out.println("Account was unlinked remotely");
	        }
	    }
	    if (mDatastoreManager == null) {
	        // Account isn't linked yet, use local datastores
	        mDatastoreManager = DbxDatastoreManager.localManager(mAccountManager);
	        // Show link button
	        mLinkButton.setVisibility(View.VISIBLE);
	    }
		
	}//onCreate

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_upload:
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	            DbxAccount account = mAccountManager.getLinkedAccount();
	            try {
	                // Migrate any local datastores to the linked account
	                mDatastoreManager.migrateToAccount(account);
	                // Now use Dropbox datastores
	                mDatastoreManager = DbxDatastoreManager.forAccount(account);
	                
	                mLinkButton.setText("Unlink from Dropbox");
	            } catch (DbxException e) {
	                e.printStackTrace();
	            }
	        } else {
	            // Link failed or was cancelled by the user
	        	//mAccountManager.unlink();
	        	//mLinkButton.setText("Link with Dropbox");
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	public void takePicture(View view)
	{
		Intent intent = new Intent(this, TakePicture.class);
		startActivity(intent);
	}
	
	//Map Button OnClick
	public void sendMessage (View view) {
		//Do something in response to Button01
		Intent intent = new Intent(this, DisplayMapActivity.class);
		startActivity(intent);
	}
	
	public void gallery(View view)
	{
		Intent intent = new Intent(this,Gallery.class);
		startActivity(intent);
	}
	
}
