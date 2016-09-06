package com.leokomarov.jamstreamer.audio_player;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        Log.v("AudioParser", "URL: " + urlString);

	    try {
	        URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        conn.connect();
	        is = conn.getInputStream();
		} catch (Exception e) {
            Log.e("AudioParser", "Exception on connect: " + e.getMessage());
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			is.close();
			json = sb.toString();
            jObj = new JSONObject(json);
		} catch (Exception e) {
            Log.e("AudioParser", "Exception after reading data: " + e.getMessage());
		}

    	JSONArray results;
    	Bitmap albumImage = null;
    	try {
            results = jObj.getJSONArray(TAG_RESULTS);
            JSONObject trackInfo = results.getJSONObject(0);

            String imageURL = trackInfo.getString(TAG_ALBUM_IMAGE);
            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
            double screenWidth = displayMetrics.widthPixels;
            double screenHeight = displayMetrics.heightPixels;
            double screenDensityDPI = displayMetrics.densityDpi;
            double screenDiagonal = Math.sqrt((screenWidth * screenWidth) + (screenHeight * screenHeight)) / screenDensityDPI;

            if (screenDiagonal <= 3.0) {
                imageURL = imageURL.replace("300.jpg", "200.jpg");
            } else if (screenDiagonal >= 6.85 && screenDiagonal < 9.0 || screenDensityDPI >= 300.0 && screenDensityDPI < 400.0) {
                imageURL = imageURL.replace("300.jpg", "400.jpg");
            } else if (screenDiagonal >= 9.0 || screenDensityDPI >= 400.0) {
                imageURL = imageURL.replace("300.jpg", "500.jpg");
            }

            albumImage = BitmapFactory.decodeStream((InputStream) new URL(imageURL).getContent());
            albumImageStore = albumImage;
        } catch (Exception e){
            Log.e("AudioParser", "Exception while getting bitmap: " + e.getMessage());
		}
    	return albumImage;
    }
			   
	@Override
    protected void onPostExecute(Bitmap albumImage) {
		AudioPlayer.albumArt.setImageBitmap(albumImage);
    }
    	
}