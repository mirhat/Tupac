package com.nebula.takeaphoto;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.R.integer;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.StaticLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

// Defining DialogFragment class to show the place details with photo
public class ImagesDownloader implements Runnable{
	Place mPlace = null;
	ArrayList<Bitmap> slike = null;
	Bitmap bitmap = null;
	String url = "https://maps.googleapis.com/maps/api/place/photo?";
	public ImagesDownloader(){
		
	}

	public ArrayList<Bitmap> getImages(Place place) {
		this.mPlace = place;		
		slike = new ArrayList();
			
		if(mPlace!=null){
			Photo[] photos = mPlace.mPhotos;				
			String key = "key=AIzaSyBU2duM8aCe9xlzCwYXSM18rL8GMv7TMfo";
			String sensor = "sensor=true";
			String maxw = "maxwidth=1600";
			String maxh = "maxheight=1600";
			url = url + "&" + key + "&" + sensor + "&" + maxw + "&" + maxh;					
				
			// Traversing through all the photoreferences
			String oldUrl = url;
			
			for(int i=0;i<photos.length;i++){				
				String photoReference = "photoreference="+photos[i].mPhotoReference;
				
				// URL for downloading the photo from Google Services
				url = oldUrl;
				url = url + "&" + photoReference;		
				Thread t = new Thread(this);
				t.start();
				try {
					t.join();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				slike.add(bitmap);	
			} 	
		}
		
		return slike;
		
	}	
	
	private Bitmap downloadImage(String strUrl) throws IOException{
		Bitmap bitmap=null;
        InputStream iStream = null;
        try{
            URL url = new URL(strUrl);
            Log.d("strUrl = ", strUrl);
            
            /** Creating an http connection to communcate with url */
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            /** Connecting to url */
     //       urlConnection.connect();

            /** Reading data from url */
            iStream = urlConnection.getInputStream();

            /** Creating a bitmap from the stream returned from the url */
            bitmap = BitmapFactory.decodeStream(iStream);

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
        }
        return bitmap;
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
        	// Starting image download
            bitmap = downloadImage(url);
        }catch(Exception e){
            Log.d("Background Task",e.toString());
        }
	}
}