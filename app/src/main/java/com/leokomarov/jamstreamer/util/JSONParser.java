package com.leokomarov.jamstreamer.util;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser extends AsyncTask<String, Void, JSONObject>  {
    static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";

    public interface CallbackInterface {
        void onRequestCompleted(JSONObject json);
    }

    private CallbackInterface mCallback;

    public JSONParser(CallbackInterface callback) {
        mCallback = callback;
    }

	@Override
    protected JSONObject doInBackground(String... urls) {
		String urlString = urls[0];
        Log.v("JSONParser", "URL: " + urlString);

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();

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
          Log.e("JSONParser", "Exception on connect: " + e.getMessage());
        }

		return jObj;
    }
			   
    @Override
    protected void onPostExecute(JSONObject jObj) {
    	mCallback.onRequestCompleted(jObj);
     }
    	
}