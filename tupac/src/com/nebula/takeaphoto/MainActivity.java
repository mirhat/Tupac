package com.nebula.takeaphoto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
		ActionBar.TabListener, LocationListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	// GoogleMap
	GoogleMap mGoogleMap;
	
	// LinearLayout in first tab
	ScrollView mScrollView;
	

	// Stores near by places
	Place[] mPlaces = null;

	// A String array containing place types sent to Google Place service
	String[] mPlaceType = new String[]{"mosque", "restaurant", "school"};

	// A String array containing place types displayed to user
	String[] mPlaceTypeName = null;

	// The location at which user touches the Google Map
	LatLng mLocation = null;

	// Links marker id and place object
	HashMap<String, Place> mHMReference = new HashMap<String, Place>();

	// Current Place
	Place place = null;

	// First tab fragment
	PlaceholderFragment placeholderFragment = null;

	// Second tab fragment
	MapFragment mapFragment = null;
	
	// Specifies the drawMarker() to draw the marker with default color
	private static final float UNDEFINED_COLOR = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		placeholderFragment = new PlaceholderFragment();
		mapFragment = new MapFragment();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		
		Thread front = new Thread(new PopulateFrontRunnable());
		front.start();

	}
	

	// Method for centring map and getting location
	private void initiateMap() {
		// Getting Google Play availability status
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());

		if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();

		} else { // Google Play Services are available

			// Making zoom controls
			mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

			// Removing link to real google maps when user selects place
			mGoogleMap.getUiSettings().setMapToolbarEnabled(true);

			// Enabling MyLocation in Google Map
			mGoogleMap.setMyLocationEnabled(true);

			// Getting LocationManager object from System Service
			// LOCATION_SERVICE
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// Creating a criteria object to retrieve provider
			Criteria criteria = new Criteria();

			// Getting the name of the best provider
			String provider = locationManager.getBestProvider(criteria, true);

			// Getting Current Location
			Location location = locationManager.getLastKnownLocation(provider);

			if (location != null) {
				mLocation = new LatLng(location.getLatitude(),
						location.getLongitude());
				onLocationChanged(location);
			}
			locationManager.requestLocationUpdates(provider, 20000, 0, this);
		}
		// Marker click listener
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                // If touched at User input location
                if(!mHMReference.containsKey(marker.getId()))
                    return false;

                // Getting place object corresponding to the currently clicked Marker
                place = mHMReference.get(marker.getId());
                
                Thread t1 = new Thread(new PlaceDetailsRunnable());
                t1.start();
                try {
					t1.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                
                ImagesDownloader imagesDownloader = new ImagesDownloader();
                Intent intent = new Intent(getApplicationContext(), ImagesActivity.class);
                ArrayList<Bitmap> imagesArrayList = imagesDownloader.getImages(place);
                
                try{
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + "place.ser");
                    if (!file.exists()) {
                    	file.createNewFile();
					}
                    if(file.exists())
                    {
                         FileOutputStream fo = new FileOutputStream(file);             
                         ObjectOutputStream out = new ObjectOutputStream(fo);
                         out.writeObject(place);
                         out.close();
                         fo.close();
                    }    
                }
                catch(Exception e){
                	Log.d("Serialization exception ", e.toString());
                }

        		startActivity(intent);
        
                return false;
            }
        });
	}

	// Method for finding places of certain types
	private void initiatePlaces(String[] types) {

		mGoogleMap.clear();		
		
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location="+mLocation.latitude+","+mLocation.longitude);
        sb.append("&radius=5000");
        sb.append("&types=");
        for (int i = 0; i < types.length-1; i++) {
			sb.append(types[i]+"|");
		}
        sb.append(types[types.length-1]);
        
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyBU2duM8aCe9xlzCwYXSM18rL8GMv7TMfo");


        // Creating a new non-ui thread task to download Google place json data
        PlacesTask placesTask = new PlacesTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        placesTask.execute(sb.toString());
	}

	// Method for putting markers on map and returns connection between marker
	// and place
	private HashMap<String, Place> drawMarkers(Place[] places) {
		return null;
	}

	private void populatePlacesListView(Place[] places) {
		
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
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return placeholderFragment.newInstance();
			} else {
				return mapFragment.newInstance();
			}
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	private class PlaceholderFragment extends Fragment {
		public PlaceholderFragment newInstance() {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			mScrollView = (ScrollView) rootView;
			return rootView;
		}
	}

	private class MapFragment extends Fragment {
		public MapFragment newInstance() {
			MapFragment fragment = new MapFragment();
			Bundle args = new Bundle();
			fragment.setArguments(args);
			return fragment;
		}

		public MapFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_map, container, false);
			// Getting reference to the SupportMapFragment
			SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager()
					.findFragmentById(R.id.map);

			// Getting Google Map
			mGoogleMap = fragment.getMap();
			initiateMap();
			initiatePlaces(mPlaceType);
			return v;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		// Getting latitude of the current location
		double latitude = location.getLatitude();

		// Getting longitude of the current location
		double longitude = location.getLongitude();

		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);

		// Showing the current location in Google Map
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

		// Zoom in the Google Map
		mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(12));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
	
	// Class for downloading place details
    private class PlaceDetailsRunnable implements Runnable{

		@Override
		public void run() {
			PlaceDetailsJSONParser.updatePlaceWithDetails(place);		
		}
    }
    
    /** A class, to download Google Places */
    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                Log.d("URL:",url[0]);
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of ParserTask
            parserTask.execute(result);
        }

    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, Place[]>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected Place[] doInBackground(String... jsonData) {


            Place[] places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();

            try{
                jObject = new JSONObject(jsonData[0]);
                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(Place[] places){

            mPlaces = places;
			
            for(int i=0;i< places.length  ;i++){

                Place place = places[i];

                // Getting latitude of the place
                double lat = Double.parseDouble(place.mLat);

                // Getting longitude of the place
                double lng = Double.parseDouble(place.mLng);

                LatLng latLng = new LatLng(lat, lng);

                Marker m = drawMarker(latLng,UNDEFINED_COLOR);

                // Adding place reference to HashMap with marker id as HashMap key
                // to get its reference in infowindow click event listener
                mHMReference.put(m.getId(), place);               
                
            }
        }

    }

    /**
     * Drawing marker at latLng with color
     */
    private Marker drawMarker(LatLng latLng,float color){
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        markerOptions.position(latLng);

        if(color != UNDEFINED_COLOR)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));

        // Placing a marker on the touched position
        Marker m = mGoogleMap.addMarker(markerOptions);

        return m;

    }
    
    /** A method to download json data from argument url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);


            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    
    
    
    // Class for populating first tab with nearby places
    private class PopulateFrontRunnable implements Runnable{

    	Place p1 = null;
		@Override
		public void run() {
			while (mPlaces == null) {
			}	
			runOnUiThread(new Runnable() {
			    public void run() {
			    	LinearLayout mLinearLayout = (LinearLayout) mScrollView.findViewById(R.id.frontLinearLayout);
			    	mLinearLayout.removeAllViews();
			    	for (Place p : mPlaces) {
			    		p1 = p;
			    		Thread t1 = new Thread(new PomRunnable());
			    		t1.start();
			    		try {
							t1.join();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			    		
			    		
			    		
						View listPlace = getLayoutInflater().inflate(R.layout.list_place, null);
						ImageView picture = (ImageView) listPlace.findViewById(R.id.frontImageControl);
						TextView title = (TextView) listPlace.findViewById(R.id.nazivFrontText);
						TextView type = (TextView) listPlace.findViewById(R.id.tipFrontText);
						if (p.mPlaceName != null) {
							title.setText(p.mPlaceName);
						}
						if (p.mPhotos.length > 0) {
							ImagesDownloader i = new ImagesDownloader();
							Bitmap resorce = i.getImage(p.mPhotos[0].mPhotoReference);
							if (resorce != null) {
								picture.setImageBitmap(resorce);
							}
						}
						mLinearLayout.addView(listPlace);
					}
			    }
			});
			
			
			
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private class PomRunnable implements Runnable{

			@Override
			public void run() {
				PlaceDetailsJSONParser.updatePlaceWithDetails(p1);
				
			}
			
		}
		
    }
 
}
