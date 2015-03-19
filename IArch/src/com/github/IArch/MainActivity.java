package com.github.IArch;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
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
import android.widget.Button;
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
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
	

	private static final String appKey = "fapxgsf7glvwkb0";
	private static final String appSecret = "1swwbsarfhraqab";
    
	static final int REQUEST_LINK_TO_DBX = 0;
	
	private Button mLinkButton;
	static DbxAccountManager mAccountManager;
	static DbxDatastoreManager mDatastoreManager;
	
	private String[] navDrawerItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
	
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
		case R.id.action_upload:
			Toast.makeText(this, "This will sync eventually!", 
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.action_export:
			System.out.println("START EXPORTING");
			export();
			Toast.makeText(this, "Data Exported!", 
					Toast.LENGTH_LONG).show();
			String longFileName = ChooserFragment.fileName.toString();
			String[] shortFileName = longFileName.split("/");
			System.out.println(shortFileName[6]);
			System.out.println("FINISHED EXPORTING");
			//export();
			return true;
		case R.id.action_settings:
			Toast.makeText(this, "No settings yet", 
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
public boolean export(){
		
		String longFileName = ChooserFragment.fileName.toString();
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
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mAccountManager.hasLinkedAccount()) {
		    showLinkedView();
		    doDropboxStuff();
		} else {
			showUnlinkedView();
		}
	}
	
	private void setUpNavDrawer()
	{
		mTitle = "Home";
		mDrawerTitle = getTitle();
		navDrawerItems = getResources().getStringArray(R.array.nav_drawer_items_array);
		// Adapt Login/Logout text to whether user is connected to Dropbox
		/*if (mAccountManager.hasLinkedAccount())
		{
			navDrawerItems[4] = "Login";
		}*/
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

    private void showLinkedView() {
    	mLinkButton = MainFragment.mLinkButton;
        mLinkButton.setText("Unlink from Dropbox");
        //navDrawerItems[4] = "Logout";
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, navDrawerItems));
    }

    private void showUnlinkedView() {
    	mLinkButton = MainFragment.mLinkButton;
    	mLinkButton.setText("Connect to Dropbox");
    	//navDrawerItems[4] = "Login";
    	//mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, navDrawerItems));
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
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	        	doDropboxStuff();
	        } else {
	            // Link failed or was cancelled by the user
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	private void doDropboxStuff() {
		DbxAccount account = mAccountManager.getLinkedAccount();
        try {
            // Migrate any local datastores to the linked account
            //mDatastoreManager.migrateToAccount(account);
            // Now use Dropbox datastores
            mDatastoreManager = DbxDatastoreManager.forAccount(account);
            
        } catch (DbxException e) {
            e.printStackTrace();
        }
        
     // Set up the datastore manager
	    if (mAccountManager.hasLinkedAccount()) {
	        try {
	            // Use Dropbox datastores
	            mDatastoreManager = DbxDatastoreManager.forAccount(mAccountManager.getLinkedAccount());
	        } catch (DbxException.Unauthorized e) {
	            System.out.println("Account was unlinked remotely");
	        }
	    }
	    if (mDatastoreManager == null) {
	        // Account isn't linked yet, use local datastores
	        mDatastoreManager = DbxDatastoreManager.localManager(mAccountManager);
	    }
	    /*
	    //sync datastores just in case back button was pressed too soon
	    DbxDatastore datastore;
		try {
			datastore = mDatastoreManager.openDefaultDatastore();
			//sync datastore
			datastore.sync();
			
			//close datastore
			datastore.close();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    */
	}
	
	public void takePicture(View view)
	{
		if (mAccountManager.hasLinkedAccount()) {
		Intent intent = new Intent(this, TakePicture.class);
		startActivity(intent);
		}
		else {
			Toast.makeText(MainActivity.this, "Error : Not connected to Dropbox", 
					Toast.LENGTH_LONG).show();
		}
	}
	
	//Map Button OnClick
	public void displayMap (View view) {
		//Do something in response to Button01
		Intent intent = new Intent(this, DisplayMapActivity.class);
		startActivity(intent);
	}
	
	public void gallery(View view)
	{
		//Intent intent = new Intent(this,Chooser.class);
		//startActivity(intent);
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
	
	public void dropboxButtonClick(View view)
	{
		onClickLinkToDropbox();
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
        break;
        case 1: // Map
        	displayMap(view);
        break;
        case 2: // Project Management
        	gallery(view);
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


	
}
