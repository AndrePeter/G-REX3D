package com.andredittrich.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.andredittrich.dataresource.R;
import com.andredittrich.surface3d.GOCADConnector;
import com.andredittrich.surface3d.OGLLayer;

public class GREX3DActivity extends Activity {

	private HelloOpenGLES20SurfaceView mGLView;
	private GOCADConnector connect3D = new GOCADConnector();
	public static OGLLayer tsobj;
	Button rotate_pan_button;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("timetest", "timetest");

		String intentData = null;
		String intentType = null;
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			intentData = extras.getString(getString(R.string.TSObject));
			intentType = extras.getString("ResourceType");
		}
		
		getTSObject(intentData, intentType);

		
		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		mGLView = new HelloOpenGLES20SurfaceView(this);

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		mGLView.mDensity = displayMetrics.density;

//		setContentView(mGLView);
		FrameLayout frame = new FrameLayout(this);
		frame.addView(mGLView);
		
//		setContentView(R.layout.main);
		
		LinearLayout lin = new LinearLayout(this);
		
		
		rotate_pan_button = new Button(this);
		rotate_pan_button.setText("Pan");
		
		rotate_pan_button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (HelloOpenGLES20Renderer.pan) {
					HelloOpenGLES20Renderer.mAngleX = 0.0f;
					HelloOpenGLES20Renderer.mAngleY = 0.0f;
					HelloOpenGLES20Renderer.pan = false;
					rotate_pan_button.setText("pan");
				} else {
					HelloOpenGLES20Renderer.mdX = 0.0f;
					HelloOpenGLES20Renderer.mdY = 0.0f;
					HelloOpenGLES20Renderer.pan = true;
					rotate_pan_button.setText("Rotate");
				}
			}
		});
		
		lin.addView(rotate_pan_button);
		
		
		frame.addView(lin);
		
		setContentView(frame);
	}

	private void getTSObject(String intentData, String intentType) {
		try {
			BufferedReader in = null;
			if (intentType.equalsIgnoreCase("WFS") ) { 
				in = new BufferedReader(new StringReader(intentData));
			} else if (intentType.equalsIgnoreCase("SDCARD") ) {				
				in = new BufferedReader(new FileReader(intentData));
			}

			Log.d("vorher", "vorher");
			tsobj = connect3D.readTSObject(in);
			Log.d("layername", tsobj.getName());
			Log.d("color", Integer.toString(tsobj.getColor()));
			Log.d("nachher", "nachher");

			//	important: close the stream for every file
			
			in.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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