package com.andredittrich.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.opengl.GLSurfaceView;
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
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.andredittrich.dataresource.FeatureTypeSelectionActivity;
import com.andredittrich.dataresource.R;

import com.andredittrich.surface3d.CoordinateConversion;
import com.andredittrich.surface3d.CoordinateTrafo;
import com.andredittrich.surface3d.DatumParams;
import com.andredittrich.surface3d.GOCADConnector;
import com.andredittrich.surface3d.OGLLayer;

public class ARActivity extends Activity implements SensorEventListener {

	
	private static final String TAG = ARActivity.class.getSimpleName();
	private static ARSurfaceView mGLView;
	private static GOCADConnector connect3D = new GOCADConnector();
	public static OGLLayer tsobj;
	private static SensorManager mSensorManager;
	private float[] rotvec = new float[3];
	public static float[] RotMat = new float[16];
	private FrameLayout frame;
	public static VerticalSeekBar myZoomBar;
	private static Switch s1;

	private String intentData = null;
	private String intentType = null;
	// Camera variables
	public static Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;
	int defaultCameraId;

	// Location variables
	private static LocationManager manager;
	private static LocationListener listener;
	private LocationProvider lp;
	private TextView textview;
	private static String providerName;

	// variables to hold "Landeskoordinaten" and geographic coordinates
	private double longitude = 0.0;
	private double latitude = 0.0;
	private double altitude;
	CoordinateTrafo ct;
	public static int epsg;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tsobj = GREX3DActivity.tsobj;
		
		Log.d("EPSG", Integer.toString(epsg));
		ct = new CoordinateTrafo(epsg);
//
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		initListeners();
		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		mGLView = new ARSurfaceView(this);
//
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mGLView.mDensity = displayMetrics.density;

		createLayout();

		manager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Provider mit grober Auflï¿½sung
		// und niedrigen Energieverbrauch
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);

		// Namen ausgeben
		providerName = manager.getBestProvider(criteria, false);

		Log.d("???" + TAG, providerName);
		// LocationListener-Objekt erzeugen
		manager.isProviderEnabled(providerName);

		setContentView(frame);
		
		listener = new LocationListener() {
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				Log.d(TAG, "onStatusChanged()");
				Log.d(TAG,
						Boolean.toString(manager.isProviderEnabled(provider)));

			}

			public void onProviderEnabled(String provider) {
				Log.d(TAG, "onProviderEnabled()");
				
				textview.setText("enabled");
				
			}

			public void onProviderDisabled(String provider) {
				Log.d(TAG, "onProviderDisabled()");
				textview.setText("disabled");
				
//				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
				Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}

			public void onLocationChanged(Location location) {

				if (location != null) {
					Log.d(TAG, "onLocationChanged()");
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					altitude = location.getAltitude();
					String s = "Breite: " + latitude + "\nLänge: " + longitude
							+ "\nHöhe: " + altitude + "\nGenauigkeit: "
							+ location.getAccuracy();
					textview.setText(s);
				} else {
					Location lastKnownLocation = manager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

					latitude = lastKnownLocation.getLatitude();
					longitude = lastKnownLocation.getLongitude();
					altitude = lastKnownLocation.getAltitude();

					String s = "Breite: " + latitude + "\nLänge: " + longitude
							+ "\nHöhe: " + altitude + "\nGenauigkeit: "
							+ lastKnownLocation.getAccuracy();
					textview.setText(s);
				}
//				double[] gk = cc.latLon2GK(latitude, longitude);
				double[] transformedCoordinate = ct.transformCoordinate(latitude, longitude, altitude);

//				InterpolateCoordinates();
				Log.d("rechtswert ", Double.toString(transformedCoordinate[0]));
				Log.d("hochwert ", Double.toString(transformedCoordinate[1]));
//				mGLView.requestRender();

			}
				
		};
////		 manager.requestLocationUpdates(providerName, 0, 0,
//		 listener);
		// manager.
		manager.requestLocationUpdates(providerName, 0, 0, listener);
//		setContentView(frame);
	}

	private void getTSObject(String intentData, String intentType) {
		try {
			BufferedReader in = null;
			if (intentType.equalsIgnoreCase("WFS")) {
				in = new BufferedReader(new StringReader(intentData));
			} else if (intentType.equalsIgnoreCase("SDCARD")) {
				in = new BufferedReader(new FileReader(intentData));
			}

			Log.d("vorherAR", "vorher");
			tsobj = connect3D.readTSObject(in);
			Log.d("layername AR", tsobj.getName());
			Log.d("colorAR", Integer.toString(tsobj.getColor()));
			Log.d("nachherAR", "nachher");

			// important: close the stream for every file

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
		manager.removeUpdates(listener);
	}

	@Override
	protected void onResume() {
		super.onResume();

//		if (!AR) {
//			resetUI();
//		} else {
//			setARprefs();
//		}
//		
//		// The following call resumes a paused rendering thread.
//		// If you de-allocated graphic objects for onPause()
//		// this is a good place to re-allocate them.
//
		mGLView.onResume();
		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
		mSensorManager.registerListener(this,
		 mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
		 SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		mGLView.onPause();
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
		mSensorManager.unregisterListener(this);
		manager.removeUpdates(listener);
	}
	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		setARprefs();
	}
	

	private void createLayout() {

		frame = new FrameLayout(this);
		mPreview = new Preview(this);
		s1 = new Switch(this);
		s1.setChecked(true);
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
				if (!isChecked) {
					finish();
				}
			}
		});
//		mPreview.mSurfaceView.setVisibility(SurfaceView.INVISIBLE);
		frame.addView(mPreview);
		frame.addView(mGLView);
//
		RelativeLayout rel = new RelativeLayout(this);
		LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rel.setLayoutParams(params);

		myZoomBar = new VerticalSeekBar(this);
		myZoomBar.setMax((int) ARRenderer.xExtent);
		Log.d("xExtent", Float.toString(ARRenderer.xExtent));
		Log.d("setMax", Float.toString(myZoomBar.getMax()));
		myZoomBar.setProgress(myZoomBar.getMax());
		myZoomBar.setEnabled(false);
		myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
		RelativeLayout.LayoutParams zoomBarParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		zoomBarParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		zoomBarParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		textview = new TextView(this);
		textview.setText("Start");
		RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//
		rel.addView(s1);
		rel.addView(myZoomBar, zoomBarParams);
		rel.addView(textview, textParams);
		
		frame.addView(rel);

	}

	protected static void setARprefs() {
		manager.requestLocationUpdates(providerName, 0, 0, listener);		
		ARRenderer.eyeZ = ARRenderer.xExtent;
		mPreview.mSurfaceView.setVisibility(SurfaceView.VISIBLE);
		myZoomBar.setMax((int) ARRenderer.xExtent);
		myZoomBar.setProgress(myZoomBar.getMax());
		myZoomBar.setEnabled(false);
//		try {
//			mSensorManager.registerListener(ARActivity.class.newInstance(),
//					mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
//					SensorManager.SENSOR_DELAY_FASTEST);
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
//		InterpolateCoordinates();
		
	}
	
	public static void InterpolateCoordinates() {
		// TODO Auto-generated method stub
		double x0 = connect3D.getMinX() + connect3D.getCorrectx();
		double y0 = connect3D.getMinY() + connect3D.getCorrecty();
		double x1 = connect3D.getMaxX() + connect3D.getCorrectx();
		double y1 = connect3D.getMaxY() + connect3D.getCorrecty();
		
		double dy = y1-y0;
//		Log.d("dy", Double.toString(dy));
		double dx = x1-x0;
		double m = dy/dx;
		double t = y1 - m*x1;
		double x = x0;
		for (float y = (float) y0; y<=y1; y= y+0.3f) {
			
			x = (y-t)/m;
			Log.d("dx", Float.toString((float) (x - connect3D.getCorrectx())));
			Log.d("dy", Float.toString((float) (y - connect3D.getCorrecty())));
			Log.d("dz", Float.toString((float) (1080.0 - connect3D.getCorrectz())));
			
			
			ARRenderer.eyeX = (float) (x - connect3D.getCorrectx());
			ARRenderer.eyeY = (float) (y - connect3D.getCorrecty());
			ARRenderer.eyeZ = (float) (1080.0 - connect3D.getCorrectz());
		}						
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
		/// if device ALWAYS LANDSCAPE !!!!!!
//		SensorManager.remapCoordinateSystem(RotMat, SensorManager.AXIS_Y,
//				SensorManager.AXIS_MINUS_X, RotMat);
//		mGLView.requestRender();

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

	public static void removeLocUpdates() {
		// TODO Auto-generated method stub
		manager.removeUpdates(listener);
	}

}