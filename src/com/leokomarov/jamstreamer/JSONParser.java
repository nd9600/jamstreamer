package com.leokomarov.jamstreamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class JSONParser extends AsyncTask<String, Void, JSONObject>  {

    static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	private static String DEBUG = "JSONParser";
	
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
		} catch (SocketTimeoutException e) {
			e.printStackTrace();    
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
    protected JSONObject doInBackground(String... urls) {
    	String url = urls[0];            
        return getJSONFromUrl(url);
    }
	
	public interface CallbackInterface {
        public void onRequestCompleted(JSONObject json);
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