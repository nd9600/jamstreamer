package com.leokomarov.jamstreamer.media_player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class AudioParser extends AsyncTask<String, Void, Bitmap> {
    InputStream is = null;
	JSONObject jObj = null;
	String json = "";
	private static final String TAG_RESULTS = "results";
	private static final String TAG_ALBUM_IMAGE = "album_image";
	protected static Bitmap albumImageStore;

	@Override
    protected Bitmap doInBackground(String... parameters) {
	    String urlString = parameters[0];    	
	    try {
	        URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        conn.connect();
	        is = conn.getInputStream();
		} catch (NullPointerException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
		}

		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
		}
		
    	JSONArray results = null;
    	Bitmap albumImage = null;
    	try {
    		results = jObj.getJSONArray(TAG_RESULTS);
    		JSONObject trackInfo = results.getJSONObject(0);   	
    		
    		String imageURL = trackInfo.getString(TAG_ALBUM_IMAGE);
    		double screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    		double screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    		double screenDensityDPI = Resources.getSystem().getDisplayMetrics().densityDpi;
            double screenDiagonal = Math.sqrt((screenWidth * screenWidth) + (screenHeight * screenHeight)) / screenDensityDPI;

			if (screenDiagonal <= 3.0 ){
				imageURL = imageURL.replace("300.jpg", "200.jpg");
			}
			else if (screenDiagonal >= 6.85 && screenDiagonal < 9.0 || screenDensityDPI >= 300.0 && screenDensityDPI < 400.0){
				imageURL = imageURL.replace("300.jpg", "400.jpg");
			}
			else if (screenDiagonal >= 9.0 || screenDensityDPI >= 400.0){
				imageURL = imageURL.replace("300.jpg", "500.jpg");
			}

   			albumImage = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
   			albumImageStore = albumImage;
    	} catch (NullPointerException e) {
		} catch (JSONException e) {
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		} catch (RuntimeException e) {
		}
    	return albumImage;
    }
			   
	@Override
    protected void onPostExecute(Bitmap albumImage) {
		AudioPlayer.albumArt.setImageBitmap(albumImage);
    }
    	
}