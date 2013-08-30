package com.leokomarov.jamstreamer.artists;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;

public class ArtistsSearch extends Activity {
	public final static String INTENT_TAG = "com.leokomarov.jamstreamer.artists.NAME";

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artists_0main);
               
        //Needs fixed, complains about API level if <11 in manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        findViewById(R.id.artistsSearchButton).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        	    sendArtistName(v);
        	}
        });  
	}
	
	public void sendArtistName(View v){
		Intent intent = new Intent(this, ArtistsParser.class);
        EditText editText = (EditText) findViewById(R.id.artistsByNameField);
        String artistName = editText.getText().toString();
        if (artistName.length() >= 2){
            intent.putExtra(INTENT_TAG, artistName);
            startActivity(intent);	
        }
        else {
        	Toast.makeText(getApplicationContext(), "You must enter two or more characters", Toast.LENGTH_SHORT).show();
        }

	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	        switch (item.getItemId()) {
	        case android.R.id.home: 
	            onBackPressed();
	            return true;
	        }
	    return super.onOptionsItemSelected(item);
	}	

}