package com.anmark.grund;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.anmark.grund.DBAdapter.Row;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlacesMapActivityV2 extends Activity {


	private static final int MAP_ZOOMLEVEL = 14;
	private GoogleMap mMap;
	static String delimiter = "DELIMITER";
	public static String KEY_REFERENCE = "reference"; // id of the place
	private double user_latitude;
	private double user_longitude;
	private PlacesList nearPlaces;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//GoogleMapOptions options = new GoogleMapOptions();
		// Getting intent data
		Intent i = getIntent();
		if(i.getExtras() != null){
			// get nearplaces list
			nearPlaces = (PlacesList) i.getSerializableExtra("near_places");
			// user gps latitude and longitude
			user_latitude = i.getDoubleExtra("user_latitude", 0.0);
			user_longitude = i.getDoubleExtra("user_longitude", 0.0);        
		}
		setContentView(R.layout.activity_places_map_v2);	
		setUpMapIfNeeded();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}
	
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapV2)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// The Map is verified. It is now safe to manipulate the map.

				DBAdapter db = new DBAdapter(getApplicationContext());
				db.open();
				//accessDB( placeID,  name);

				//final LatLngBounds.Builder builder = new LatLngBounds.Builder();
				//Number of latitude and longitude in for loop
				for(Place p : nearPlaces.results){
					String comment = "";
					Double rating = 0.0;
					if(db.rowExists(p.id)){
						Row r = db.getRow(p.id);
						comment = r.getComment();
						rating = r.getRating();
					}

					final LatLng placePos = new LatLng(p.getGeometry().location.lat,p.getGeometry().location.lng);
					//builder.include(placePos);
					mMap.addMarker(new MarkerOptions()
					.position(placePos)					                          
					// marker workaround title is place reference
					.title(p.reference)
					// marker workaround snippet will contain custom info window data
					.snippet(p.name + " " + delimiter + " " + comment + " " + delimiter + " " + rating)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
				}

				db.close();

				mMap.setInfoWindowAdapter(new InfoWindowAdapter() {

					@Override
					public View getInfoWindow(Marker arg0) {

						return null;
					}

					@Override
					public View getInfoContents(Marker marker) {
						View myContentsView = getLayoutInflater().inflate(R.layout.place_custom_info_window, null);
						TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));				
						TextView tvComment = ((TextView)myContentsView.findViewById(R.id.comment));
						TextView tvRating = ((TextView)myContentsView.findViewById(R.id.rating));

						// marker workaround first split will contain comment, second rating
						String [] temp = marker.getSnippet().split(delimiter);
						tvTitle.setText(temp[0]);
						tvComment.setText("Comment: " + temp[1]);

						tvRating.setText("Rating: " + temp[2]);

						return myContentsView;
					}
				});

				mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick(Marker marker) {
						//action to be taken after click on info window
						System.out.println("infowindowcklicked");
						Intent in = new Intent(getApplicationContext(),
								SinglePlaceActivity.class);

						// marker workaround title is place reference
						in.putExtra(KEY_REFERENCE, marker.getTitle());
						startActivity(in);
						finish();
					}
				});

				mMap.setMyLocationEnabled(true);

				//GPSTracker gps =  SplashScreenActivity.gps;
				//Location myLocation = gps.getLocation();

				//set map type
				mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

				// Get latitude of the current location
				//double latitude = myLocation.getLatitude();

				// Get longitude of the current location
				//double longitude = myLocation.getLongitude();

				// Create a LatLng object for the current location
				final LatLng latLng = new LatLng(user_latitude, user_longitude);      

				mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

					public void onCameraChange(CameraPosition arg0) {
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOMLEVEL));
						mMap.setOnCameraChangeListener(null);
					}
				});
				/*        
			    // Show the current location in Google Map        
			    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

			    // Zoom in the Google Map
			    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
			    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("That's you!"));
				 */
			}
		}
	}
}
