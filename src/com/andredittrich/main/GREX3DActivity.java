package com.andredittrich.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.andredittrich.dataresource.R;
import com.andredittrich.surface3d.GOCADConnector;
import com.andredittrich.surface3d.OGLLayer;

public class GREX3DActivity extends Activity {

	private HelloOpenGLES20SurfaceView mGLView;
	private GOCADConnector connect3D = new GOCADConnector();
	public static OGLLayer tsobj;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String intentData = null;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			intentData = extras.getString(getString(R.string.TSObject));
		}
		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		try {
			BufferedReader in = new BufferedReader(new FileReader(intentData));
			
			Log.d("datei", in.readLine());

			tsobj = connect3D.readTSObject(in);
			
			Log.d("layername", tsobj.getName());
			Log.d("color", Integer.toString(tsobj.getColor()));
			/*
			 * important: close the stream for every file
			 */
			in.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mGLView = new HelloOpenGLES20SurfaceView(this);

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		mGLView.mDensity = displayMetrics.density;
		setContentView(mGLView);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		mGLView.onResume();
	}

}