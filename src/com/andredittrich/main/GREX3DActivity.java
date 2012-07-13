package com.andredittrich.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.andredittrich.dataresource.R;

import com.andredittrich.surface3d.GOCADConnector;
import com.andredittrich.surface3d.OGLLayer;

public class GREX3DActivity extends Activity implements
SensorEventListener {

	private HelloOpenGLES20SurfaceView mGLView;
	private GOCADConnector connect3D = new GOCADConnector();
	public static OGLLayer tsobj;
	private SensorManager mSensorManager;
	private float[] rotvec = new float[3];
	public static float[] RotMat = new float[16];
	private FrameLayout frame;
	private Button b1;
	private Button b2;
	private VerticalSeekBar myZoomBar;
	private Switch s1;
	
	//Camera variables
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;
	int defaultCameraId;
	
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

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		initListeners();
		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		mGLView = new HelloOpenGLES20SurfaceView(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mGLView.mDensity = displayMetrics.density;
		
//		RelativeLayout frame = new RelativeLayout(this);
//		frame.addView(mGLView);
//		
//		RelativeLayout rel = new RelativeLayout(this);
//		LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		rel.setLayoutParams(params);
//		
//		final Button b1 = new Button(this);		
//		Drawable d1 = getResources().getDrawable( R.drawable.custom_button );		
//		b1.setBackgroundDrawable(d1);
//		LayoutParams paramsbutton1 = new RelativeLayout.LayoutParams(128, 128);
//		
//		paramsbutton1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//	    b1.setLayoutParams(paramsbutton1);
//		
//		final Button b2 = new Button(this);
//		Drawable d2 = getResources().getDrawable( R.drawable.custom_button_2 );		
//		b2.setBackgroundDrawable(d2);
//		b2.setEnabled(false);
//		
//		
//		LayoutParams paramsbutton2 = new RelativeLayout.LayoutParams(128, 128);
//		paramsbutton2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//	    b2.setLayoutParams(paramsbutton2);
//		
//	    b1.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				
//				b1.setEnabled(false);
//				b2.setEnabled(true);
//
//					HelloOpenGLES20Renderer.mdX = 0.0f;
//					HelloOpenGLES20Renderer.mdY = 0.0f;
//					HelloOpenGLES20Renderer.pan = true;				
//					
//			}
//		});
//				
//		
//		b2.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				
//				b1.setEnabled(true);
//				b2.setEnabled(false);
//				
//					HelloOpenGLES20Renderer.mAngleX = 0.0f;
//					HelloOpenGLES20Renderer.mAngleY = 0.0f;
//				HelloOpenGLES20Renderer.pan = false;
//			}
//		});
//		
//		rel.addView(b1);
//		rel.addView(b2);		
//
//		frame.addView(rel);
		
		createLayout();
		
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
		// HelloOpenGLES20Renderer.pan = false;
		// The following call pauses the rendering thread.
		// If your OpenGL application is memory intensive,
		// you should consider de-allocating objects that
		// consume significant memory here.
		mGLView.onPause();
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		mSensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
super.onResume();
		
		resetUI();
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.
		
		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	private void resetUI() {
		// TODO Auto-generated method stub
		mGLView.onResume();
		mPreview.mSurfaceView.setVisibility(SurfaceView.INVISIBLE);
		b1.setEnabled(false);
		b2.setEnabled(true);
		HelloOpenGLES20Renderer.pan = true;
		b1.setVisibility(Button.VISIBLE);
		b2.setVisibility(Button.VISIBLE);
		myZoomBar.setVisibility(VerticalSeekBar.INVISIBLE);
		HelloOpenGLES20Renderer.AR = false;
//		mGLView.requestRender();
		s1.setChecked(false);
		mGLView.requestRender();
		
	}

	private void createLayout() {
		
		frame = new FrameLayout(this);
		mPreview = new Preview(this);
		s1 = new Switch(this);
		s1.setText("");
		s1.setTextOff("Interactive");
		s1.setTextOn("Augmented Reality");
		LayoutParams paramsswitch1 = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		paramsswitch1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		paramsswitch1.addRule(RelativeLayout.CENTER_HORIZONTAL);
		s1.setLayoutParams(paramsswitch1);

		s1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					
					HelloOpenGLES20Renderer.AR = true;
					b1.setVisibility(Button.INVISIBLE);
					b2.setVisibility(Button.INVISIBLE);
					mPreview.mSurfaceView.setVisibility(SurfaceView.VISIBLE);
					myZoomBar.setVisibility(VerticalSeekBar.VISIBLE);
					mSensorManager.registerListener(GREX3DActivity.this,
							mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
							SensorManager.SENSOR_DELAY_FASTEST);
					
				} else {
					mSensorManager.unregisterListener(GREX3DActivity.this);
					resetUI();
//					HelloOpenGLES20Renderer.AR = false;
//					mPreview.mSurfaceView.setVisibility(SurfaceView.INVISIBLE);
//					myZoomBar.setVisibility(VerticalSeekBar.INVISIBLE);
//					b1.setVisibility(Button.VISIBLE);
//					b2.setVisibility(Button.VISIBLE);
					
				}
			}
		});
		mPreview.mSurfaceView.setVisibility(SurfaceView.INVISIBLE);
		frame.addView(mPreview);
		frame.addView(mGLView);

		RelativeLayout rel = new RelativeLayout(this);
		LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rel.setLayoutParams(params);
		
		b1 = new Button(this);
		Drawable d1 = getResources().getDrawable(R.drawable.custom_button);
		b1.setBackgroundDrawable(d1);
		LayoutParams paramsbutton1 = new RelativeLayout.LayoutParams(128, 128);
		paramsbutton1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		paramsbutton1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		b1.setLayoutParams(paramsbutton1);

		b2 = new Button(this);
		Drawable d2 = getResources().getDrawable(R.drawable.custom_button_2);
		b2.setBackgroundDrawable(d2);
		b2.setEnabled(false);

		LayoutParams paramsbutton2 = new RelativeLayout.LayoutParams(128, 128);
		paramsbutton2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
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

		myZoomBar = new VerticalSeekBar(this);
		myZoomBar.setVisibility(VerticalSeekBar.INVISIBLE);
		myZoomBar.setMax(100);
		myZoomBar.setProgress(100);
		myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
		RelativeLayout.LayoutParams zoomBarParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		zoomBarParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		zoomBarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		

		rel.addView(s1);

		rel.addView(myZoomBar, zoomBarParams);

		frame.addView(rel);
		
	}

public void initListeners() {
	mSensorManager.registerListener(this,
			mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
			SensorManager.SENSOR_DELAY_FASTEST);
}

public void onAccuracyChanged(Sensor sensor, int accuracy) {
	// TODO Auto-generated method stub
	
}

public void onSensorChanged(SensorEvent event) {
	System.arraycopy(event.values, 0, rotvec, 0, 3);
	SensorManager.getRotationMatrixFromVector(RotMat, event.values);
	SensorManager.remapCoordinateSystem(RotMat, SensorManager.AXIS_Y,
			SensorManager.AXIS_MINUS_X, RotMat);
	mGLView.requestRender();
	
}

private OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new OnSeekBarChangeListener() {
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// updateDataOnZoom();
		// camScreen.invalidate();
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// Ignore
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		// updateDataOnZoom();
		// camScreen.invalidate();
	}
};

}