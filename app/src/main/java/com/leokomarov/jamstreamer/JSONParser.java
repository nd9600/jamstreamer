package com.leokomarov.jamstreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class JSONParser extends AsyncTask<String, Void, JSONObject>  {
    static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

	@Override
    protected JSONObject doInBackground(String... urls) {
		String urlString = urls[0];  
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
		  System.out.println("Exception:" + e.getMessage());
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
			System.out.println("Exception:" + e.getMessage());
		}

		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			System.out.println("Exception:" + e.getMessage());
		}
		return jObj;
    }
	
	public interface CallbackInterface {
        void onRequestCompleted(JSONObject json);
    }
	
	private CallbackInterface mCallback;

    public JSONParser(CallbackInterface callback) {
        mCallback = callback;
    }
			   
    @Override
    protected void onPostExecute(JSONObject jObj) {
    	mCallback.onRequestCompleted(jObj);
     }
    	
}