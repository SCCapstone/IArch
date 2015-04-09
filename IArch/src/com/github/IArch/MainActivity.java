package com.github.IArch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxFileSystem.PathListener;
import com.dropbox.sync.android.DbxFileSystem.SyncStatusListener;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxSyncStatus;
import com.dropbox.sync.android.DbxTable;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
	

	private static final String appKey = "fapxgsf7glvwkb0";
	private static final String appSecret = "1swwbsarfhraqab";
    
	static final int REQUEST_LINK_TO_DBX = 0;
	
	static DbxAccountManager mAccountManager;
	static DbxDatastoreManager mDatastoreManager;
	DbxFileSystem dbxFs;
	
	private String[] navDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    String projectName;
    SyncStatusListener syncListener;
    PathListener pathListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
		
		setUpNavDrawer();
		
		mAccountManager = DbxAccountManager.getInstance(getApplicationContext(), appKey, appSecret);

		// Set up the datastore manager
	    if (mAccountManager.hasLinkedAccount()) {
	        try {
	           	// Use Dropbox datastores
	           	mDatastoreManager = DbxDatastoreManager.forAccount(mAccountManager.getLinkedAccount());
	       	} catch (DbxException.Unauthorized e) {
	           	System.out.println("Account was unlinked remotely");
	       	}
	    
	        if (mDatastoreManager == null) {
	        	// Account isn't linked yet, use local datastores
	        	mDatastoreManager = DbxDatastoreManager.localManager(mAccountManager);
	    	}

			try {
				dbxFs = DbxFileSystem.forAccount(mAccountManager.getLinkedAccount());
			} catch (Unauthorized e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//set up dropbox listeners
			setUpListeners();
	    }
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		if (mAccountManager.hasLinkedAccount()) {
		    showLinkedView();
		} else {
			showUnlinkedView();
		}
		setUpListeners();
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
		if (mAccountManager.hasLinkedAccount()) {
			if (syncListener != null) {
				dbxFs.removeSyncStatusListener(syncListener);
				System.out.println("Dropbox sync listener removed");
			}
			if (pathListener != null) {
				dbxFs.removePathListenerForAll(pathListener);
				System.out.println("Dropbox path listener removed");
			}
		}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		// The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
       if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
       }
       
       switch (item.getItemId()) {
		case R.id.action_sync:
			sync();
			if (mAccountManager.hasLinkedAccount()) {
				Toast.makeText(this, "Syncing Project: " + projectName, 
					Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.action_export:
			System.out.println("START EXPORTING");
			export();
			if (mAccountManager.hasLinkedAccount()) {
				Toast.makeText(this, "Exporting Project: " + projectName, 
					Toast.LENGTH_LONG).show();
				System.out.println("FINISHED EXPORTING");
			}
			return true;
		case R.id.action_settings:
			Toast.makeText(this, "No settings yet", 
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.action_share:
			share();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void setUpListeners() {
		syncListener = new DbxFileSystem.SyncStatusListener() {
			@Override
			public void onSyncStatusChange(DbxFileSystem fs) {
				DbxSyncStatus fsStatus;
				try {
					fsStatus = fs.getSyncStatus();
					
					if (fsStatus.anyInProgress()) {
						//Show syncing indicator
						
					}
				} catch (DbxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				//Set syncing indicator based on current sync status
			
		}; 
		pathListener = new DbxFileSystem.PathListener() {
			
			@Override
			public void onPathChange(DbxFileSystem fs, DbxPath registeredPath, Mode registeredMode) {
				System.out.println("Dropbox path listener was called");
				
			}
		};
	}
	
	public void sync() {
		if (mAccountManager.hasLinkedAccount()) {
			String longFileName = ChooserFragment.folderName.toString();
			String[] shortFileName = longFileName.split("/");
			projectName = shortFileName[6];
			
			dbxFs.addSyncStatusListener(syncListener);
			DbxPath remotePath = new DbxPath(shortFileName[6]);
			dbxFs.addPathListener(pathListener, remotePath, DbxFileSystem.PathListener.Mode.PATH_OR_CHILD);
			System.out.println("Dropbox listeners started");
						
			//open datastore and get fresh data
			DbxDatastore datastore = null;
			try {
				datastore = MainActivity.mDatastoreManager.openDatastore(shortFileName[6]);
				datastore.sync();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			datastore.close();
		} else {
			Toast.makeText(this, "Error: Not connected to Dropbox", 
					Toast.LENGTH_LONG).show();
		}
	}
	
	public boolean export() {
		if (mAccountManager.hasLinkedAccount()) {
			String longFileName = ChooserFragment.folderName.toString();
			String[] shortFileName = longFileName.split("/");
			projectName = shortFileName[6];
			File path = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES) + "/iArch/" + shortFileName[6]);
			DbxPath remotePath = new DbxPath(shortFileName[6] + "/" + shortFileName[6] + ".csv");
		    File[] imageFiles = path.listFiles();
		    String finalString = "";
		    
			try {
				//if remote file already exists, delete it before exporting new file
				if (dbxFs.exists(remotePath)) {
					dbxFs.delete(remotePath);
				}
				DbxFile exportFile = dbxFs.create(remotePath);
				
				try {
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
		} else {
			Toast.makeText(this, "Error: Not connected to Dropbox", 
					Toast.LENGTH_LONG).show();
		}
		
		return false;
		
	}
	
	public void share()
	{
		String longFileName = ChooserFragment.folderName.toString();
		String[] shortFileName = longFileName.split("/");
		projectName = shortFileName[6];
		String fileName = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + "/iArch/" + shortFileName[6];
		
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
		emailIntent.setType("image/jpeg");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {""}); 
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "subject"); 
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "body");
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+fileName));
		startActivity(Intent.createChooser(emailIntent, "Sharing Options"));
	}
	
	private void setUpNavDrawer()
	{
		mTitle = "Home";
		mDrawerTitle = getTitle();
		navDrawerItems = getResources().getStringArray(R.array.nav_drawer_items_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, navDrawerItems));
        
        // Handle navigation click events
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  // host Activity 
                mDrawerLayout,         // DrawerLayout object 
                R.drawable.ic_drawer,  // nav drawer image to replace 'Up' caret 
                R.string.drawer_open,  // "open drawer" description for accessibility 
                R.string.drawer_close  // "close drawer" description for accessibility 
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	        	DbxAccount account = mAccountManager.getLinkedAccount();
	        	try {
	        		//Migrate any local datastores to the linked account
	        		mDatastoreManager = DbxDatastoreManager.localManager(mAccountManager);
	        		mDatastoreManager.migrateToAccount(account);
	        		//Now use dropbox datastores
	        		mDatastoreManager = DbxDatastoreManager.forAccount(account);
	        		setUpListeners();
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
	
	public void takePicture(View view)
	{
		if (mAccountManager.hasLinkedAccount()) {
			// Create new fragment and transaction
			Fragment newFragment = new TakePictureFragment();
			FragmentTransaction transaction = getFragmentManager().beginTransaction();

			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack
			transaction.replace(R.id.container, newFragment);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}
		else {
			Toast.makeText(MainActivity.this, "Error : Not connected to Dropbox", 
					Toast.LENGTH_LONG).show();
		}
	}
	
	
	public void displayMap (View view) {
		// Create new fragment and transaction
		Fragment newFragment = new DisplayMapFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.container, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}
	
	public void gallery(View view)
	{
		// Create new fragment and transaction
		Fragment newFragment = new ChooserFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.container, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}
	
	// The click listener for ListView in the navigation drawer 
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position, view);
        }
    }
    
    private void selectItem(int position, View view) {
    	
    	// update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
        // Click actions
        switch(position) {
        case 0: // Camera
        	takePicture(view);
        	mTitle = "Camera";
        break;
        case 1: // Map
        	displayMap(view);
        	mTitle = "Map";
        break;
        case 2: // Project Management
        	gallery(view);
        	mTitle = "Projects";
        break;
        case 3: // Options
        	Toast.makeText(MainActivity.this, "Options", 
					Toast.LENGTH_LONG).show();
        break;
        case 4: // Login/Logout
        	onClickLinkToDropbox();
        break;
        default:
        }
    }

    private void onClickLinkToDropbox() {
	
    	if (mAccountManager.hasLinkedAccount()) {
    		//if already linked to dropbox and button is clicked, unlink
            mAccountManager.unlink();
            showUnlinkedView();
        } else {
    		mAccountManager.startLink((Activity)MainActivity.this, REQUEST_LINK_TO_DBX);
    		showLinkedView();
    	}
    }

    private void showLinkedView() {
    	MainFragment.mLinkButton.setText("Unlink from Dropbox");
    	navDrawerItems[4] = "Logout";	
    }

    private void showUnlinkedView() {
    	MainFragment.mLinkButton.setText("Connect to Dropbox");
    	navDrawerItems[4] = "Login";
    }
    
}
