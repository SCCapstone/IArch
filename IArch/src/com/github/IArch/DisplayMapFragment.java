package com.github.IArch;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dropbox.sync.android.DbxDatastore;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;



public class DisplayMapFragment extends Fragment implements
	com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks, 
	com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener, 
	com.google.android.gms.location.LocationListener,
	OnMyLocationButtonClickListener {

	MapView mMapView;
	private GoogleMap googleMap;
	View view;

	private GoogleApiClient mGoogleApiClient;
	float zoom = 16;
	int zoomCounter = 0;
	DbxDatastore datastore;

	//These settings are the same as the settings for the map. They will in fact give you updates
	// at the maximal rates currently possible.
	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000)         // 5 seconds
			.setFastestInterval(16)    // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		zoomCounter = 0;
		// inflate and return the layout
		view = inflater.inflate(R.layout.fragment_display_map, container, false);
		mMapView = (MapView) view.findViewById(R.id.mapView);
		mMapView.onCreate(savedInstanceState);

		mMapView.onResume();// needed to get the map to display immediately

		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		googleMap = mMapView.getMap();

		// Perform any camera updates here
		setUpMapIfNeeded();
		setUpGoogleApiClientIfNeeded();
		mGoogleApiClient.connect();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpGoogleApiClientIfNeeded();
		mMapView.onResume();
	}	

	@Override
	public void onPause() {
		super.onPause();
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	private void setUpMapIfNeeded() {
		// Check if we were successful in obtaining the map.
		if (googleMap != null) {
			googleMap.setMyLocationEnabled(true);
			googleMap.setOnMyLocationButtonClickListener(this);
			googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			setupLastKnown();
		}
	}

	private synchronized void setUpGoogleApiClientIfNeeded() {
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();
		}	
	}

	@Override
	public boolean onMyLocationButtonClick() {
		Toast.makeText(getActivity(), "Locating...", Toast.LENGTH_SHORT).show();
		// Return false so that we don't consume the event and the default behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}

	@Override
	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		LatLng LatLong = new LatLng(latitude, longitude);
		if (zoomCounter < 5) {
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLong));
			googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom), 2000, null);
		}
		zoomCounter++;
		System.out.println("zoomCounter: " + zoomCounter);
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				mGoogleApiClient,
				REQUEST,
				this);  // LocationListener
	}

	void setupLastKnown() {
		//acquire a reference to system location manager
		LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
					googleMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLong));
				}
			}
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
	}

}