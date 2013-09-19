package com.leokomarov.jamstreamer.media_player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;

public class AudioParser extends AsyncTask<String, Void, Bitmap> {

	private static String DEBUG = "AudioParser";
    static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private static final String TAG_RESULTS = "results";
	private static final String TAG_ALBUM_IMAGE = "album_image";
	private static final String TAG_ARTIST_NAME = "artist_name";
	private static final String TAG_ALBUM_NAME = "album_name";

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
			Log.e("AudioParser", "NullPointerException: " + e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			Log.e("AudioParser", "UnsupportedEncodingException: " + e.getMessage(), e);
		} catch (ClientProtocolException e) {
			Log.e("AudioParser", "ClientProtocolException: " + e.getMessage(), e);
		} catch (IOException e) {
			Log.e("AudioParser", "IOException: " + e.getMessage(), e);
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
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
		Log.e(DEBUG, "Error parsing data " + e.toString());
		}
		
    	JSONArray results = null;
    	Bitmap albumImage = null;
    	try {
    		results = jObj.getJSONArray(TAG_RESULTS);
    		JSONObject trackInfo = results.getJSONObject(0);
    		
    		String imageURL = trackInfo.getString(TAG_ALBUM_IMAGE);
    		String artistName = trackInfo.getString(TAG_ARTIST_NAME);
    		String albumName = trackInfo.getString(TAG_ALBUM_NAME);
    		String artistAndAlbum = artistName + " - " + albumName;
    		String audioPlayer_artistAndAlbum = parameters[1];
    		BitmapDrawable albumImageDrawable = ((BitmapDrawable)AudioPlayer.albumArt.getDrawable());
    		
    		double screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    		double screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    		double screenDensityDPI = Resources.getSystem().getDisplayMetrics().densityDpi;
            double screenDiagonal = Math.sqrt((screenWidth * screenWidth) + (screenHeight * screenHeight)) / screenDensityDPI;
    		   		
			if (screenDiagonal <= 3.0 ){
				imageURL = imageURL.replace("300.jpg", "200.jpg");
			}
			else if (screenDiagonal >= 7.5 && screenDiagonal < 9.0){
				imageURL = imageURL.replace("300.jpg", "400.jpg");
			}
			else if (screenDiagonal >= 9.0){
				imageURL = imageURL.replace("300.jpg", "500.jpg");
			}

     		if (albumImageDrawable == null || ! audioPlayer_artistAndAlbum.equals(artistAndAlbum) ){
    			albumImage = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
        	}		
		} catch (JSONException e) {
			Log.e(DEBUG, "JSONException " + e.toString());
		} catch (MalformedURLException e) {
			Log.e("MalformedURLException", "Wrong URL:  " + e.toString());
		} catch (IOException e) {
			Log.e("IOException ", "IOException: " + e.toString());
		}
    	return albumImage;
    }
			   
	@Override
    protected void onPostExecute(Bitmap albumImage) {
		if(albumImage != null){
			AudioPlayer.albumArt.setImageBitmap(albumImage);
		}
    }
    	
}