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
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

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

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mGLView.mDensity = displayMetrics.density;
		
		RelativeLayout frame = new RelativeLayout(this);
		frame.addView(mGLView);
		
		RelativeLayout rel = new RelativeLayout(this);
		LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rel.setLayoutParams(params);
		
		final Button b1 = new Button(this);		
		Drawable d1 = getResources().getDrawable( R.drawable.custom_button );		
		b1.setBackgroundDrawable(d1);
		LayoutParams paramsbutton1 = new RelativeLayout.LayoutParams(128, 128);
		
		paramsbutton1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	    b1.setLayoutParams(paramsbutton1);
		
		final Button b2 = new Button(this);
		Drawable d2 = getResources().getDrawable( R.drawable.custom_button_2 );		
		b2.setBackgroundDrawable(d2);
		b2.setEnabled(false);
		
		
		LayoutParams paramsbutton2 = new RelativeLayout.LayoutParams(128, 128);
		paramsbutton2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	    b2.setLayoutParams(paramsbutton2);
		
	    b1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				b1.setEnabled(false);
				b2.setEnabled(true);

					HelloOpenGLES20Renderer.mdX = 0.0f;
					HelloOpenGLES20Renderer.mdY = 0.0f;
					HelloOpenGLES20Renderer.pan = true;				
					
			}
		});
				
		
		b2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				b1.setEnabled(true);
				b2.setEnabled(false);
				
					HelloOpenGLES20Renderer.mAngleX = 0.0f;
					HelloOpenGLES20Renderer.mAngleY = 0.0f;
				HelloOpenGLES20Renderer.pan = false;
			}
		});
		
		rel.addView(b1);
		rel.addView(b2);
		
//		rotate_pan_button = new Button(this);
//		rotate_pan_button.setText("Pan");
//		
//		rotate_pan_button.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				if (HelloOpenGLES20Renderer.pan) {
//					HelloOpenGLES20Renderer.mAngleX = 0.0f;
//					HelloOpenGLES20Renderer.mAngleY = 0.0f;
//					HelloOpenGLES20Renderer.pan = false;
//					rotate_pan_button.setText("pan");
//				} else {
//					HelloOpenGLES20Renderer.mdX = 0.0f;
//					HelloOpenGLES20Renderer.mdY = 0.0f;
//					HelloOpenGLES20Renderer.pan = true;
//					rotate_pan_button.setText("Rotate");
//				}
//			}
//		});
//		
//		lin.addView(rotate_pan_button);
		
		
		frame.addView(rel);
		
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