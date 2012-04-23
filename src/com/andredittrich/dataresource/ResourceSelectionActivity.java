package com.andredittrich.dataresource;


import com.andredittrich.main.GREX3DActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ResourceSelectionActivity extends Activity {

	private Button sdcard;
	private Button wfs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resourceselection);
		
		sdcard = (Button) findViewById(R.id.SDCard);
				
		sdcard.setOnClickListener(new OnClickListener() {
			
			public void onClick (View v) {
				//TODO: Implement Intent for getting data from SD Card
				sdcard.setText("OK");
				Intent intent = new Intent(ResourceSelectionActivity.this, GREX3DActivity.class);				
				startActivity(intent);
			}
		});
		wfs = (Button) findViewById(R.id.WFS);
		wfs.setOnClickListener(new OnClickListener() {
			
			public void onClick (View v) {
				//TODO: Implement Intent for getting data from WFS
				wfs.setText("OK");
				Intent intent = new Intent(ResourceSelectionActivity.this, WFSSelectionActivity.class);				
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
