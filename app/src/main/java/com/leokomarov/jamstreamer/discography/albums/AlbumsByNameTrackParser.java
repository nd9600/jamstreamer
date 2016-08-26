package com.leokomarov.jamstreamer.discography.albums;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class AlbumsByNameTrackParser extends AsyncTask<String, Void, JSONObject> {
    Context context;

    public interface CallbackInterface {
        void onTrackRequestCompleted(JSONObject json);
    }

    private CallbackInterface mCallback;

    public AlbumsByNameTrackParser(CallbackInterface callback, Context context) {
        mCallback = callback;
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        JSONObject jObj = null;
        try {
            InputStream is;
            String json;
            String myURL = urls[0];
            URL url = new URL(myURL);
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
            System.out.println("Exception:" + e.getMessage());
        }

        return jObj;
    }

    @Override
    protected void onPostExecute(JSONObject jObj) {
        mCallback.onTrackRequestCompleted(jObj);
    }
}
