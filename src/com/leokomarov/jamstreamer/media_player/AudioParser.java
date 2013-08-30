package com.leokomarov.jamstreamer.media_player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

class Pair
{
	public JSONObject jObj;
	public Bitmap bitmap;
}

public class AudioParser extends AsyncTask<String, Void, Pair> {

	private static String DEBUG = "AudioParser";
    static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private static final String TAG_RESULTS = "results";
	private static final String TAG_ARTIST_NAME = "artist_name";
	private static final String TAG_ALBUM_NAME = "album_name";
	private static final String TAG_ALBUM_IMAGE = "album_image";
	private static final String TAG_TRACK_NAME = "name";
	private static final String TAG_TRACK_DURATION = "duration";
	Context context;
	
	public JSONObject getJSONFromUrl(String myurl) {
		try {
	        URL url = new URL(myurl);
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
	
		return jObj;
	}

	@Override
    protected Pair doInBackground(String... urls) {
		String url = urls[0];    	
    	jObj = getJSONFromUrl(url);
    	Pair AudioParserPair = new Pair();
    	JSONArray results = null;
    	try {
    		results = jObj.getJSONArray(TAG_RESULTS);
    		JSONObject trackInfo = results.getJSONObject(0);
    		String imageURL = trackInfo.getString(TAG_ALBUM_IMAGE);
    		
    		//Crashes when finding density, probably needs to be an activity
    		/*
    		double screenDensity = context.getResources().getDisplayMetrics().density;
    		Log.v(DEBUG,"Screen density is " + screenDensity);
			if (screenDensity > 1.5 && screenDensity < 3.0){
				imageURL = imageURL.replace("imagesize=300", "imagesize=400");
			}
			else if (screenDensity <= 1.0 ){
				imageURL = imageURL.replace("imagesize=300", "imagesize=200");
				
			}
			else if (screenDensity >= 3.0){
				imageURL = imageURL.replace("imagesize=300", "imagesize=500");
			}
			*/
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
	    	AudioParserPair.jObj = jObj;
	    	AudioParserPair.bitmap = bitmap;
		} catch (JSONException e) {
			Log.e(DEBUG, "JSONException " + e.toString());
		} catch (MalformedURLException e) {
			Log.e("MalformedURLException", "Wrong URL:  " + e.toString());
		} catch (IOException e) {
			Log.e("IOException ", "IOException: " + e.toString());
		} 	
    	
        return AudioParserPair;
    }
			   
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
    protected void onPostExecute(Pair AudioParserPair) {
		jObj = AudioParserPair.jObj;
		Bitmap bitmap = AudioParserPair.bitmap;
    	JSONArray results = null;
				try {
					results = jObj.getJSONArray(TAG_RESULTS);
					JSONObject trackInfo = results.getJSONObject(0);
							
					String artistName = trackInfo.getString(TAG_ARTIST_NAME);
					String albumName = trackInfo.getString(TAG_ALBUM_NAME);
					String trackName = trackInfo.getString(TAG_TRACK_NAME);
					String trackDuration = trackInfo.getString(TAG_TRACK_DURATION);
					long trackDurationLong = Long.valueOf(trackDuration);
					String timeString = String.format(Locale.US, "%d:%02d", TimeUnit.SECONDS.toMinutes(trackDurationLong),trackDurationLong % 60);
					
					AudioPlayer.songTitleLabel.setText(trackName + " - " + artistName);
					AudioPlayer.albumLabel.setText(albumName);
					AudioPlayer.songThumbnailImageView.setImageBitmap(bitmap);
					AudioPlayer.songTotalDurationLabel.setText(timeString);
				} catch (JSONException e) {
					Log.e(DEBUG, "JSONException " + e.toString());
				}
     }
    	
}
