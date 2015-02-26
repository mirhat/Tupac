package com.nebula.takeaphoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlaceJSONParser {
	
	/** Receives a JSONObject and returns a list */
	public Place[] parse(JSONObject jObject){		
		
		JSONArray jPlaces = null;
		try {			
			/** Retrieves all the elements in the 'places' array */
			jPlaces = jObject.getJSONArray("results");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/** Invoking getPlaces with the array of json object
		 * where each json object represent a place
		 */
		return getPlaces(jPlaces);
	}
	
	
	private Place[] getPlaces(JSONArray jPlaces){
		int placesCount = jPlaces.length();		
		Place[] places = new Place[placesCount];	

		/** Taking each place, parses and adds to list object */
		for(int i=0; i<placesCount;i++){
			try {
				/** Call getPlace with place JSON object to parse the place */
				places[i] = getPlace((JSONObject)jPlaces.get(i));				
				

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return places;
	}
	
	/** Parsing the Place JSON object */
	private Place getPlace(JSONObject jPlace){

		Place place = new Place();
		
		try {
			// Extracting Place name, if available
			if(!jPlace.isNull("name")){				
				place.mPlaceName = jPlace.getString("name");
			}
			
			// Extracting Place Vicinity, if available
			if(!jPlace.isNull("vicinity")){
				place.mVicinity = jPlace.getString("vicinity");
			}	
			
			// Extracting Place Id
			place.mPlaceId = jPlace.getString("place_id");
			place.mLat = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
			place.mLng = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
			
		} catch (JSONException e) {			
			e.printStackTrace();
			Log.d("EXCEPTION", e.toString());
		}		
		return place;
	}
    /** A method to download json data from argument url */
    public static String downloadUrl(String strUrl) throws IOException {
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
}