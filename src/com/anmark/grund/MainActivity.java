package com.anmark.grund;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class MainActivity extends Activity {

	// flag for Internet connection status
	Boolean isInternetPresent = false;

	// Connection detector class
	ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	// Google Places
	GooglePlaces googlePlaces;

	// Places List
	PlacesList nearPlaces;

	// GPS Location
	GPSTracker gps;

	// Button
	Button btnShowOnMap;

	// Progress dialog
	ProgressDialog pDialog;

	// Places Listview
	ListView lv;

	// ListItems data
	ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();


	// KEY Strings
	public static String KEY_REFERENCE = "reference"; // id of the place
	public static String KEY_NAME = "name"; // name of the place
	public static String KEY_VICINITY = "vicinity"; // Place area name

	private Typeface tf;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// get customfont
		tf = Typeface.createFromAsset(getAssets(), "fonts/slapstick.ttf");

		// Getting listview
		lv = (ListView) findViewById(R.id.list);

		// button show on map
		btnShowOnMap = (Button) findViewById(R.id.btn_show_map);
		btnShowOnMap.setTypeface(tf);

		//TODO: Bad fix, pass arguments to activity instead
		nearPlaces = SplashScreenActivity.nearPlaces;
		gps = SplashScreenActivity.gps;

		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 

		// calling background Async task to load Google Places
		// After getting places from Google all the data is shown in listview
		new LoadPlaces().execute();

		/** Button click event for shown on map */
		btnShowOnMap.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						PlacesMapActivityV2.class);
				// Sending user current geo location
				i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
				i.putExtra("user_longitude", Double.toString(gps.getLongitude()));

				// passing near places to map activity
				i.putExtra("near_places", nearPlaces);
				// staring activity
				startActivity(i);
			}
		});


		/**
		 * ListItem click event
		 * On selecting a listitem SinglePlaceActivity is launched
		 * */
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting values from selected ListItem

				/*
				DBAdapter db = new DBAdapter(getApplicationContext());
				db.open();
				db.deleteAllRows();
				db.close();

				 */
				
				// Starting new intent
				// Sending place refrence id to single place activity
				// place refrence id used to get "Place full details"
				String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();
				Intent in = new Intent(getApplicationContext(),
						SinglePlaceActivity.class);
				in.putExtra(KEY_REFERENCE, reference);
				startActivity(in);

			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	/**
	 * Background Async Task to Load Google places
	 * */
	class LoadPlaces extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			// creating Places class object
			googlePlaces = new GooglePlaces();
			//nearPlaces = SplashScreenActivity.nearPlaces;
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * and show the data in UI
		 * Always use runOnUiThread(new Runnable()) to update UI from background
		 * thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			//pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating parsed Places into LISTVIEW
					 * */
					// Get json response status
					//System.out.println("nearplaces status " + nearPlaces.status);
					String status = nearPlaces.status;

					// Check for all possible status
					if(status.equals("OK")){
						// Successfully got places details
						if (nearPlaces.results != null) {
							// loop through each place
							for (Place p : nearPlaces.results) {
								HashMap<String, String> map = new HashMap<String, String>();

								// Place reference won't display in listview - it will be hidden
								// Place reference is used to get "place full details"
								map.put(KEY_REFERENCE, p.reference);

								// Place name
								map.put(KEY_NAME, p.name);


								// adding HashMap to ArrayList
								placesListItems.add(map);
							}
							// list adapter

							ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,	R.layout.list_item,
									new String[] { KEY_REFERENCE, KEY_NAME}, new int[] {
									R.id.reference, R.id.name }){
								@Override
								public View getView(int pos, View convertView, ViewGroup parent){
									View v = convertView;
									if(v== null){
										LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
										v=vi.inflate(R.layout.list_item, null);
									}
									TextView tvRef = (TextView)v.findViewById(R.id.reference);
									tvRef.setText(placesListItems.get(pos).get(KEY_REFERENCE));
									//tvRef.setTypeface(tf);
									TextView tvName = (TextView)v.findViewById(R.id.name);
									tvName.setText(placesListItems.get(pos).get(KEY_NAME));
									tvName.setTypeface(tf);
									
									/* Gets distance from current location to place address but to heavy calculation and does not match
									 * google places rankby distance...
									
									String destinationAddress = "";
									
									PlaceDetails tempPlaceDetails;
									TextView tvDist = (TextView)v.findViewById(R.id.distance);

									try {
										tempPlaceDetails = googlePlaces.getPlaceDetails(placesListItems.get(pos).get(KEY_REFERENCE));
										destinationAddress = tempPlaceDetails.result.formatted_address;
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									Double latitude = gps.getLocation().getLatitude();
									Double longitude = gps.getLocation().getLongitude();
									//tvDist.setText("Distance: " + Double.toString((getDistanceInfo(latitude, longitude, destinationAddress))));
									tvDist.setTypeface(tf);*/
									return v;
								}


							};

							// Adding data into listview
							lv.setAdapter(adapter);						
						}
					}
					else if(status.equals("ZERO_RESULTS")){
						// Zero results found
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry no places found.",
								false);
					}
					else if(status.equals("UNKNOWN_ERROR"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry unknown error occured.",
								false);
					}
					else if(status.equals("OVER_QUERY_LIMIT"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry query limit to google places is reached",
								false);
					}
					else if(status.equals("REQUEST_DENIED"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured. Request is denied",
								false);
					}
					else if(status.equals("INVALID_REQUEST"))
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured. Invalid Request",
								false);
					}
					else
					{
						alert.showAlertDialog(MainActivity.this, "Places Error",
								"Sorry error occured.",
								false);
					}
				}
			});

		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
	/*	Possible future usage
	 * 	Gets distance from current location to place address but to heavy calculation and does not match
	 *  google places rankby distance...
	 * 
	  	private double getDistanceInfo(double lat1, double lng1, String destinationAddress) {
		StringBuilder stringBuilder = new StringBuilder();
		Double dist = 0.0;
		try {

			destinationAddress = destinationAddress.replaceAll(" ","%20");    
			String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" + lat1 + "," + lng1 + "&destination=" + destinationAddress + "&mode=driving&sensor=false";

			HttpPost httppost = new HttpPost(url);

			HttpClient client = new DefaultHttpClient();
			HttpResponse response;
			stringBuilder = new StringBuilder();


			response = client.execute(httppost);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject = new JSONObject(stringBuilder.toString());

			JSONArray array = jsonObject.getJSONArray("routes");

			JSONObject routes = array.getJSONObject(0);

			JSONArray legs = routes.getJSONArray("legs");

			JSONObject steps = legs.getJSONObject(0);

			JSONObject distance = steps.getJSONObject("distance");

			Log.i("Distance", distance.toString());
			dist = Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]","") );

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dist;
	}
	
*/


/*import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class MainActivity extends Activity {

	private SupportMapFragment mapFragment;
	private GoogleMap mMap;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//GoogleMapOptions options = new GoogleMapOptions();


		setContentView(R.layout.activity_main);

		setUpMapIfNeeded();
	}
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapV2))
					.getMap();

			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// The Map is verified. It is now safe to manipulate the map.
				//mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				//mMap.setMyLocationEnabled(true);

				//Location location = mMap.getMyLocation();

				//LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			    //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);

			    //mMap.animateCamera(cameraUpdate);

				// Enable MyLocation Layer of Google Map
				mMap.setMyLocationEnabled(true);

			    // Get LocationManager object from System Service LOCATION_SERVICE
			    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			    // Create a criteria object to retrieve provider
			    Criteria criteria = new Criteria();

			    criteria.setAccuracy(Criteria.ACCURACY_FINE);

			    // Get the name of the best provider
			    String provider = locationManager.getBestProvider(criteria, true);

			    // Get Current Location
			    Location myLocation = locationManager.getLastKnownLocation(provider);

			    //set map type
			    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			    // Get latitude of the current location
			    double latitude = myLocation.getLatitude();

			    // Get longitude of the current location
			    double longitude = myLocation.getLongitude();

			    // Create a LatLng object for the current location
			    LatLng latLng = new LatLng(latitude, longitude);      

			    // Show the current location in Google Map        
			    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

			    // Zoom in the Google Map
			    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
			    mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("That's you!"));
			}
		}
	}
}*/