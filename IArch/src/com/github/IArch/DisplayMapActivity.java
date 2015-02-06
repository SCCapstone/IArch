package com.github.IArch;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
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
			
			getPins();
			
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
	
	public void getPins() {
		//set up dropbox datastores
	    DbxDatastore datastore;
		try {
			datastore = MainActivity.mDatastoreManager.openDefaultDatastore();
			DbxTable tasksTbl = datastore.getTable("tasks");
			//DbxFields queryParams = new DbxFields().getString(latitude);
			DbxTable.QueryResult results = tasksTbl.query();
			System.out.println("results: " + results);
			datastore.close();
		} catch (DbxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		
		// You can customize the marker image using images bundled with
        // your app, or dynamically generated bitmaps.
        mMap.addMarker(new MarkerOptions()
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.house_flag))
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(41.889, -87.622)));
	}
	
	/**
     * Button to get current Location. This demonstrates how to get the current Location as required
     * without needing to register a LocationListener.
     
    public void showMyLocation(View view) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            String msg = "Location = "
                    + LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
	*/
}
