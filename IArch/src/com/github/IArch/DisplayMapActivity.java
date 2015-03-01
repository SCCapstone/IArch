package com.github.IArch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreInfo;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class DisplayMapActivity extends FragmentActivity
	implements
	ConnectionCallbacks,
	OnConnectionFailedListener,
	LocationListener,
	OnMyLocationButtonClickListener {

	private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private TextView mMessageView;
    private LatLng LatLong;
    float zoom = 16;
    int zoomCounter = 0;
    //Spinner spinner;
    DbxDatastore datastore;
    
    
 // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_map);
		mMessageView = (TextView) findViewById(R.id.message_text);
				
	}

	@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpGoogleApiClientIfNeeded();
        mGoogleApiClient.connect();
        
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                setupLastKnown();
                
                if (MainActivity.mAccountManager.hasLinkedAccount()) {
                	addItemsOnSpinner();
                } else {
                	hideSpinner();
                }
            }
        }
    }

    //add location of photos in database as map pins
    private void mapPins(String datastoreName) {
    	//reset map pins
    	mMap.clear();
    	if (MainActivity.mAccountManager.hasLinkedAccount()) {	
    		//open datastore and get fresh data
			try {
				datastore = MainActivity.mDatastoreManager.openDatastore(datastoreName);
				datastore.sync();
				
				//open table
				DbxTable tasksTbl = datastore.getTable("Picture_Data");
				
				//query table for results
				DbxTable.QueryResult results = tasksTbl.query();
				Iterator<DbxRecord> it = results.iterator();
				
				while (it.hasNext()) {
					DbxRecord firstResult = it.next(); 
					Double myLongitude = firstResult.getDouble("LONGITUDE");
					Double myLatitude = firstResult.getDouble("LATITUDE");
					String myFilename = firstResult.getString("LOCAL_FILENAME");
					//shorten path
					String[] splitFile = myFilename.split("/");
					LatLng myLoc = new LatLng(myLatitude,myLongitude);
					mMap.addMarker(new MarkerOptions()
						.position(myLoc)
						.title(splitFile[7]));
				}
				
				datastore.close();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	}
	}

	private void setUpGoogleApiClientIfNeeded() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.display_map, menu);
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
	public boolean onMyLocationButtonClick() {
		Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		
		mMessageView.setText("Location = " + location);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLong = new LatLng(latitude, longitude);
		if (zoomCounter == 0) {
			mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLong));
			mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 2000, null);
		}
		zoomCounter++;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                this);  // LocationListener
		
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// Do nothing
		
	}
	
	void setupLastKnown() {
		//acquire a reference to system location manager
				LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
				String locationProvider = LocationManager.GPS_PROVIDER;
				
				//check to see if last known location exists
				if (locationManager != null) {
					//set cached last known location to current location for initial state
					Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
					if (lastKnownLocation != null) {
						double lastLatitude = lastKnownLocation.getLatitude();
						double lastLongitude = lastKnownLocation.getLongitude();
						LatLng lastLatLong = new LatLng(lastLatitude, lastLongitude);
						if (zoomCounter == 0) {
							mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLong));
						}
					}
				}
				
				
	}
	
	public void addItemsOnSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        
		ArrayList<DbxDatastoreInfo> infos = new ArrayList<DbxDatastoreInfo>();
		try {
			//query database for datastore names
			infos.addAll(MainActivity.mDatastoreManager.listDatastores());
			
			for (int i=0; i<infos.size(); i++) {
				DbxDatastoreInfo data = infos.get(i);
				String id = data.id;
				list.add(id);
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(dataAdapter);
				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						System.out.println("ITEM SELECTED AT POSITION: " + position);
						String selectedItem = parent.getItemAtPosition(position).toString();
						mapPins(selectedItem);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
												
					}
					
				});
				
				
			}
		} catch (DbxException e) {
			e.printStackTrace();
		}
	}
	
	void hideSpinner() {
		TextView pins = (TextView) findViewById(R.id.pins);
		pins.setVisibility(View.GONE);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);
	}
	

	
}