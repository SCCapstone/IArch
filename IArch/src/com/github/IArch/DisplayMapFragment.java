package com.github.IArch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreInfo;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



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
	LayoutInflater mInflater;
	String myFilename;
	ViewGroup parent;
	String filePath;

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
		mInflater = inflater;
		parent = (ViewGroup) getActivity().findViewById(R.id.container);
		// inflate and return the layout
		view = inflater.inflate(R.layout.fragment_display_map, container, false);
		getActionBar().setTitle(R.string.title_fragment_display_map);
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
	
	private ActionBar getActionBar() {
	    return getActivity().getActionBar();
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
		//mMapView.onDestroy();
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
			googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
				
				@Override
				public View getInfoWindow(Marker marker) {
					//provides a view for entire info window
					String title = marker.getTitle();
					View v = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, parent, false);
					ImageView image = (ImageView) v.findViewById(R.id.mapImageView);
					TextView text = (TextView) v.findViewById(R.id.mapTextView);
					String[] splitFile = myFilename.split("/");
					filePath = splitFile[0] + "/" + splitFile[1] + "/" + splitFile[2] + "/" + 
							splitFile[3] + "/" + splitFile[4] + "/" + splitFile[5] + "/" + splitFile[6] + "/";
					System.out.println("File Path: " + filePath);
					Bitmap thumbImage = decodeSampledBitmapFromFile(filePath + title, 325, 200);
					image.setImageBitmap(thumbImage);
					text.setText(title);
					
					return v;
				}
				
				@Override
				public View getInfoContents(Marker marker) {
					//here you can customize the contents of the window but still 
					//keep the default info window frame and background
					return null;
				}
			});
			googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
				
				@Override
				public void onInfoWindowClick(Marker marker) {
					String title = marker.getTitle();
					// Create new fragment and transaction
					Fragment newFragment = new ImageDetailsFragment();
					Bundle bundle = new Bundle();
					bundle.putString("EXTRAS_FILENAME", filePath + title);
					newFragment.setArguments(bundle);
					FragmentTransaction transaction = getFragmentManager().beginTransaction();

					// Replace whatever is in the fragment_container view with this fragment,
					// and add the transaction to the back stack
					transaction.replace(R.id.container, newFragment);
					transaction.addToBackStack(null);

					// Commit the transaction
					transaction.commit();
				}
			});
			setupLastKnown();
			
			if (MainActivity.mAccountManager.hasLinkedAccount()) {
            	addItemsOnSpinner();
            } else {
            	hideSpinner();
            }
			
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

	//add location of photos in database as map pins
    private void mapPins(String datastoreName) {
    	//reset map pins
    	googleMap.clear();
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
					myFilename = firstResult.getString("LOCAL_FILENAME");
					//shorten path
					String[] splitFile = myFilename.split("/");
					LatLng myLoc = new LatLng(myLatitude,myLongitude);
					//Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(myFilename), 100, 100);
					//System.out.println("ThumbImage: " + ThumbImage);
					
					googleMap.addMarker(new MarkerOptions()
						.position(myLoc)
						.title(splitFile[7]));
						//.icon(BitmapDescriptorFactory.fromBitmap(ThumbImage))
						//.anchor(0,1));
					
					//free memory used when creating thumbnail 
					//ThumbImage.recycle();
				}
				
				datastore.close();
			} catch (DbxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	}
	}
	
    public void addItemsOnSpinner() {
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
                
        ArrayList<DbxDatastoreInfo> infos = new ArrayList<DbxDatastoreInfo>();
		try {
			//query database for datastore names
			infos.addAll(MainActivity.mDatastoreManager.listDatastores());
			
			for (int i=0; i<infos.size(); i++) {
				DbxDatastoreInfo data = infos.get(i);
				String id = data.id;
				list.add(id);
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout_map, list);
				dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_layout);
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
		//TextView pins = (TextView) view.findViewById(R.id.pins);
		//pins.setVisibility(View.GONE);
		
		Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);
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
}
