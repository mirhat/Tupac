package com.nebula.takeaphoto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Demonstrate how to populate a complex ListView with icon and text.
 * Icon images taken from icon pack by Everaldo Coelho (http://www.everaldo.com)
 */
public class ImagesActivity extends Activity {
	private List<Bitmap> images;
	private Place place;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_images);
		deserialize();
		ImagesDownloader imagesDownloader = new ImagesDownloader();
		images = imagesDownloader.getImages(place);
		populateListView();
		//registerClickCallback();
	}
	private void deserialize(){
		try
	      {
			File file = new File(Environment.getExternalStorageDirectory() + File.separator + "place.ser");
	         FileInputStream fileIn = new FileInputStream(file);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         place = (Place) in.readObject();
	         in.close();
	         fileIn.close();
	      }catch(IOException i)
	      {
	         
	         return;
	      }catch(ClassNotFoundException c)
	      {
	         
	         return;
	      }
	}

	private void populateListView() {
		ArrayAdapter<Bitmap> adapter = new MyListAdapter();
		ListView list = (ListView) findViewById(R.id.imagesListView1);
		list.setAdapter(adapter);
	}
	
	/*
	private void registerClickCallback() {
		ListView list = (ListView) findViewById(R.id.carsListView);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked,
					int position, long id) {
				
				Car clickedCar = myCars.get(position);
				String message = "You clicked position " + position
								+ " Which is car make " + clickedCar.getMake();
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
	*/
	private class MyListAdapter extends ArrayAdapter<Bitmap> {
		public MyListAdapter() {
			super(ImagesActivity.this, R.layout.list_single, images);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Make sure we have a view to work with (may have been given null)
			View itemView = convertView;
			if (itemView == null) {
				itemView = getLayoutInflater().inflate(R.layout.list_single, parent, false);
			}
			
			// Find the car to work with.
			Bitmap currentImage = images.get(position);
			
			// Fill the view
			ImageView imageView = (ImageView)itemView.findViewById(R.id.imageControl);
			imageView.setImageBitmap(currentImage);
			
			return itemView;
		}				
	}
}












