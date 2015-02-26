package com.nebula.takeaphoto;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class Place implements Parcelable, Serializable{
	// Place id
	public String mPlaceId="";
	
	// Latitude of the place
	public String mLat="";
	
	// Longitude of the place
	public String mLng="";
	
	// Place Name
	public String mPlaceName="";
	
	// Vicinity of the place
	public String mVicinity="";
	
	// Photos of the place
	// Photo is a Parcelable class
	public Photo[] mPhotos={};
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}		
	
	/** Writing Place object data to Parcel */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPlaceId);
		dest.writeString(mLat);
		dest.writeString(mLng);
		dest.writeString(mPlaceName);
		dest.writeString(mVicinity);
		dest.writeParcelableArray(mPhotos, 0);	
	}
	
	public Place(){		
	}
	
	/** Initializing Place object from Parcel object */
	private Place(Parcel in){
		this.mPlaceId = in.readString();
		this.mLat = in.readString();
		this.mLng = in.readString();
		this.mPlaceName = in.readString();
		this.mVicinity = in.readString();
		this.mPhotos = (Photo[])in.readParcelableArray(Photo.class.getClassLoader());				
	}
	
	
	/** Generates an instance of Place class from Parcel */
	public static final Creator<Place> CREATOR = new Creator<Place>(){
		@Override
		public Place createFromParcel(Parcel source) {			
			return new Place(source);
		}

		@Override
		public Place[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Place[size];
		}		
	};
}