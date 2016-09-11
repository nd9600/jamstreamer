package com.leokomarov.jamstreamer.ignored.searches;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.leokomarov.jamstreamer.R;

public class ArtistsSearch extends AppCompatActivity {
	protected static final String TAG_ARTIST_NAME = "name";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_search);
        getSupportActionBar();//.setDisplayHomeAsUpEnabled(true);
        
        findViewById(R.id.artistsSearchButton).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getApplicationContext(), ArtistsParser.class);
                EditText editText = (EditText) findViewById(R.id.artistsByNameField);
                String artistName = editText.getText().toString();
                if (artistName.length() >= 2){
                    intent.putExtra(TAG_ARTIST_NAME, artistName);
                    startActivity(intent);	
                }
                else {
                	Toast.makeText(getApplicationContext(), "You must enter two or more characters", Toast.LENGTH_SHORT).show();
                }
        	}
        });  
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