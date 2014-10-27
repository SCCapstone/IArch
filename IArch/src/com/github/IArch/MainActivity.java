package com.github.IArch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;

public class MainActivity extends ActionBarActivity {

	static final int REQUEST_LINK_TO_DBX = 0;
	
	private Button mLinkButton;
	private DbxAccountManager mAccountManager;
	private DbxDatastoreManager mDatastoreManager;
	
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
	            // Hide link button
	            mLinkButton.setVisibility(View.GONE);
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
		
	}

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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
	                // Hide link button
	                mLinkButton.setVisibility(View.GONE);
	            } catch (DbxException e) {
	                e.printStackTrace();
	            }
	        } else {
	            // Link failed or was cancelled by the user
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
}
