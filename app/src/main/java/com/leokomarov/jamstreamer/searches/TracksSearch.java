package com.leokomarov.jamstreamer.searches;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.leokomarov.jamstreamer.R;
import com.leokomarov.jamstreamer.common.TracksByName;
import com.leokomarov.jamstreamer.common.TracksByNameAdapter;

public class TracksSearch extends SherlockActivity {
	public static final String TAG_TRACK_NAME = "name";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracks_search);        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        findViewById(R.id.tracksSearchButton).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getApplicationContext(), TracksByName.class);
                EditText editText = (EditText) findViewById(R.id.tracksByNameField);
                String trackName = editText.getText().toString();
                if (trackName.length() >= 2){
                    intent.putExtra(TAG_TRACK_NAME, trackName);
                    startActivityForResult(intent, 1);	
                }
                else {
                	Toast.makeText(getApplicationContext(), "You must enter two or more characters", Toast.LENGTH_SHORT).show();
                }
        	}
        });  
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
	    	TracksByNameAdapter.TracksByNameCheckboxList.clear();
	    	TracksByNameAdapter.TracksByNameCheckboxCount = 0;
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	        int itemId = item.getItemId();
			if (itemId == android.R.id.home) {
				onBackPressed();
				return true;
			}
	    return super.onOptionsItemSelected(item);
	}	
}