package com.nebula.takeaphoto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.nu;

import android.util.Log;

public class PlaceDetailsJSONParser {
	public PlaceDetailsJSONParser() {

	}

	// This method updates Place with details! Not implemented for all details only for photos
	public static void updatePlaceWithDetails(Place place) {
		StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
		sb.append("placeid=" + place.mPlaceId);
		sb.append("&key=AIzaSyBU2duM8aCe9xlzCwYXSM18rL8GMv7TMfo");
		String data = null;

		try {
			Log.d("URL:", sb.toString());
			data = PlaceJSONParser.downloadUrl(sb.toString());

			JSONObject jPlace = null;
			jPlace = new JSONObject(data).getJSONObject("result");

			if (!jPlace.isNull("photos")) {
				JSONArray photos = null;
				photos = jPlace.getJSONArray("photos");
				
				Log.d("Slike = ", String.valueOf(photos.length()));
				place.mPhotos = new Photo[photos.length()];
				for (int i = 0; i < photos.length(); i++) {
					place.mPhotos[i] = new Photo();
					place.mPhotos[i].mWidth = ((JSONObject) photos.get(i)).getInt("width");
					place.mPhotos[i].mHeight = ((JSONObject) photos.get(i)).getInt("height");
					place.mPhotos[i].mPhotoReference = ((JSONObject) photos.get(i)).getString("photo_reference");
					JSONArray attributions = ((JSONObject) photos.get(i)).getJSONArray("html_attributions");
					place.mPhotos[i].mAttributions = new Attribution[attributions
							.length()];
					for (int j = 0; j < attributions.length(); j++) {
						place.mPhotos[i].mAttributions[j] = new Attribution();
						place.mPhotos[i].mAttributions[j].mHtmlAttribution = attributions.getString(j);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
}
