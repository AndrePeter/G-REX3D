package com.andredittrich.dataresource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.andredittrich.dataresource.DataOnSDSelection.FileListFilter;
import com.andredittrich.main.GREX3DActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ResourceSelectionActivity extends Activity {

	private Button sdcard;
	private Button wfs;
	public static final String rootDirectory = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/data";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resourceselection);

		sdcard = (Button) findViewById(R.id.SDCard);
		sdcard.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ResourceSelectionActivity.this,
						DataOnSDSelection.class);

				startActivity(intent);
			}
		});

		wfs = (Button) findViewById(R.id.WFS);
		wfs.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ResourceSelectionActivity.this,
						WFSSelectionActivity.class);
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
