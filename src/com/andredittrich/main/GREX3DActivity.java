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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
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

import com.andredittrich.dataresource.R;

import com.andredittrich.surface3d.CoordinateConversion;
import com.andredittrich.surface3d.GOCADConnector;
import com.andredittrich.surface3d.OGLLayer;

public class GREX3DActivity extends Activity implements SensorEventListener {

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
		resetUI();
	}



	private static final String TAG = GREX3DActivity.class.getSimpleName();
	private static boolean AR = false;
	private HelloOpenGLES20SurfaceView mGLView;
	private GOCADConnector connect3D = new GOCADConnector();
	public static OGLLayer tsobj;
	private SensorManager mSensorManager;
	private float[] rotvec = new float[3];
	public static float[] RotMat = new float[16];
	private FrameLayout frame;
	private Button b1;
	private Button b2;
	public static VerticalSeekBar myZoomBar;
	private Switch s1;

	// Camera variables
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;
	int defaultCameraId;

	// Location variables
	private LocationManager manager;
	private LocationListener listener;
	private LocationProvider lp;
	private TextView textview;
	private String providerName;

	// variables to hold "Landeskoordinaten" and geographic coordinates
	private double longitude = 0.0;
	private double latitude = 0.0;
	private double altitude;
	public static double rechtswert;
	public static double hochwert;
	private CoordinateConversion cc = new CoordinateConversion();

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

		Log.d("eyeZ", Float.toString(HelloOpenGLES20Renderer.eyeZ));

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		initListeners();
		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
		mGLView = new HelloOpenGLES20SurfaceView(this);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);//FEATURE_LEFT_TITLE);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		mGLView.mDensity = displayMetrics.density;

		createLayout();

		manager = (LocationManager) getSystemService(LOCATION_SERVICE);

		// Provider mit grober Auflösung
		// und niedrigen Energieverbrauch
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);

		// Namen ausgeben
		providerName = manager.getBestProvider(criteria, false);

		Log.d("???" + TAG, providerName);
		// LocationListener-Objekt erzeugen
		manager.isProviderEnabled(providerName);

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
				float[] gk = LatLon2GK(latitude, longitude, altitude);
				HelloOpenGLES20Renderer.eyeX = (float) (gk[0] - connect3D.correctx);
				HelloOpenGLES20Renderer.eyeY = (float) (gk[1] - connect3D.correcty);
				HelloOpenGLES20Renderer.eyeZ = (float) (altitude - connect3D.correctz);
				Log.d("rechtswert ", Float.toString(gk[0]));
				Log.d("hochwert ", Float.toString(gk[1]));
				mGLView.requestRender();

			}

				public float[] LatLon2GK(double B, double L, double h) {

//		double B = 49.;
//		double L = 8.;
//		double h = 0;
		double Brad = B*Math.PI/180.;
		double Lrad = L*Math.PI/180.;
		
		double Bessela = 6377397.15508;
		double Besselb = 6356078.9629;
		double Bessele2 = 0.00667437223115226;
		double WGSa = 6378137.;
		double WGSb = 6356752.31425;
		double WGSe2 = 0.00669437998863492;
		double dx = -598.1;
		double dy = -73.7;
		double dz = -418.2;
		double ex = 0.202/3600*Math.PI/180.;		
		double ey = 0.045/3600*Math.PI/180.;
		double ez = -2.455/3600*Math.PI/180.;
		double m = -6.7*0.000001+1;		
		
		double WGSN = WGSa/Math.sqrt(1-WGSe2*Math.pow(Math.sin(Brad), 2));
		double vecx = (WGSN+h)*Math.cos(Brad)*Math.cos(Lrad);
		double vecy = (WGSN+h)*Math.cos(Brad)*Math.sin(Lrad);;
		double vecz = (WGSN*(Math.pow(WGSb, 2)/Math.pow(WGSa, 2))+h)*Math.sin(Brad);
		
		double rottedx = vecx*1. + vecy*ez + vecz*-ey;
		double rottedy = vecx*-ez + vecy*1. + vecz*ex;
		double rottedz = vecx*ey + vecy*-ex + vecz*1.;
		double scalex = rottedx*m;
		double scaley = rottedy*m;
		double scalez = rottedz*m;
		double dxrot = dx*1. + dy*ez + dz*-ey;
		double dyrot = dx*-ez + dy*1. + dz*ex;
		double dzrot = dx*ey + dy*-ex + dz*1.;
		
				
		double x = scalex + dxrot;
		double y = scaley + dyrot;
		double z = scalez + dzrot;
		
		double s = Math.sqrt(x*x+y*y);
		double T = Math.atan((z*Bessela)/(s*Besselb));
		double BradGK = Math.atan( (z+Bessele2*(Math.pow(Bessela, 2)/Besselb)*Math.pow(Math.sin(T),3)) / (s-Bessele2*Bessela*Math.pow(Math.cos(T), 3)) );
		double LradGK = Math.atan(y/x);
		double BGK = BradGK*180./Math.PI;
		double LGK = LradGK*180./Math.PI;
//		double Brad = B*Math.PI/180.;		
		double n = (Bessela-Besselb)/(Bessela+Besselb);
		double e = (Math.pow(Bessela, 2)-Math.pow(Besselb, 2))/Math.pow(Bessela, 2);
		double ny = Math.sqrt((Math.pow(Bessela, 2)/Math.pow(Besselb, 2))*e*Math.pow(Math.cos(BradGK), 2));
		double t = Math.tan(BradGK);
		double NGK = Bessela/Math.sqrt(1-e*Math.pow(Math.sin(BradGK),2));
		double alpha = ((Bessela+Besselb)/2)*(1+0.25*Math.pow(n,2)+(1./64.)*Math.pow(n, 4));
		double beta = -(3./2.)*n+(9./16.)*Math.pow(n, 3)-(3./32.)*Math.pow(n, 5);
		double gamma = (15./16.)*Math.pow(n, 2)-(15./32.)*Math.pow(n, 4);
		double delta = -(35./48.)*Math.pow(n, 3)+(105./256.)*Math.pow(n, 5);
		double epsilon = (315./512.)*Math.pow(n, 4);
		double L0;
		if (Math.abs(LGK-6)<1.5) {
			L0 = 6f;
		} else if(Math.abs(LGK-9)<1.5) {
			L0 = 9f;
		} else if(Math.abs(LGK-12)<1.5) {
			L0 = 12f;
		} else {
			L0 = 15f;
		}
		double l = (LGK-L0)*Math.PI/180.;
		double rw2 = +(NGK / 6) * Math.pow(Math.cos(BradGK), 3)
				* (1 - Math.pow(t, 2) + Math.pow(ny, 2)) * Math.pow(l, 3);
		double rw1 = NGK * Math.cos(BradGK) * l;
		double hw2 = (t / 24)* NGK* Math.pow(Math.cos(BradGK), 4)* (5 - Math.pow(t, 2) + 9 * Math.pow(ny, 2) + 4 * Math.pow(ny,	4))*Math.pow(l, 4);
		System.out.println("hw2 " + Double.toString(hw2));
		double hw1 = (t / 2) * NGK * Math.pow(Math.cos(BradGK), 2) * Math.pow(l, 2);
		System.out.println("hw1 " + Double.toString(hw1));
		double arclength = alpha
				* (BradGK + beta * Math.sin(2 * BradGK) + gamma
						* Math.sin(4 * BradGK) + delta * Math.sin(6 * BradGK) + epsilon
						* Math.sin(8 * BradGK));
		float hochwert = (float) (arclength + hw1 + hw2);
		float rechtswert = (float) (rw1 + rw2 + 500000 + L0 / 3 * 1000000);
		
		System.out.println("hochwert " + Double.toString(hochwert));
		System.out.println("rechtswert " + Double.toString(rechtswert));
		
		return new float[]{rechtswert, hochwert};
	}
		};
//		 manager.requestLocationUpdates(providerName, 0, 0,
//		 listener);
		// manager.

		setContentView(frame);
	}

	private void getTSObject(String intentData, String intentType) {
		try {
			BufferedReader in = null;
			if (intentType.equalsIgnoreCase("WFS")) {
				in = new BufferedReader(new StringReader(intentData));
			} else if (intentType.equalsIgnoreCase("SDCARD")) {
				in = new BufferedReader(new FileReader(intentData));
			}

			Log.d("vorher", "vorher");
			tsobj = connect3D.readTSObject(in);
			Log.d("layername", tsobj.getName());
			Log.d("color", Integer.toString(tsobj.getColor()));
			Log.d("nachher", "nachher");

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

		if (!AR) {
			resetUI();
		} else {
			setARprefs();
		}
		
		// The following call resumes a paused rendering thread.
		// If you de-allocated graphic objects for onPause()
		// this is a good place to re-allocate them.

		mCamera = Camera.open();
		cameraCurrentlyLocked = defaultCameraId;
		mPreview.setCamera(mCamera);
		// mSensorManager.registerListener(this,
		// mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
		// SensorManager.SENSOR_DELAY_FASTEST);
	}

	private void resetUI() {
		// TODO Auto-generated method stub
		
		AR = false;
		mPreview.mSurfaceView.setVisibility(SurfaceView.INVISIBLE);
		b1.setEnabled(false);
		b2.setEnabled(true);
		HelloOpenGLES20Renderer.mdX = 0.0f;
		HelloOpenGLES20Renderer.mdY = 0.0f;
		HelloOpenGLES20Renderer.mAngleX = 0.0f;
		HelloOpenGLES20Renderer.mAngleY = 0.0f;
		HelloOpenGLES20Renderer.pan = true;
		b1.setVisibility(Button.VISIBLE);
		b2.setVisibility(Button.VISIBLE);
		myZoomBar.setVisibility(VerticalSeekBar.INVISIBLE);
		HelloOpenGLES20Renderer.AR = false;
		// mGLView.requestRender();
		s1.setChecked(false);
//		mSensorManager.unregisterListener(GREX3DActivity.this);
		manager.removeUpdates(listener);
		mGLView.onResume();
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

					setARprefs();
					// HelloOpenGLES20Renderer.AR = true;
					// b1.setVisibility(Button.INVISIBLE);
					// b2.setVisibility(Button.INVISIBLE);
					// mPreview.mSurfaceView.setVisibility(SurfaceView.VISIBLE);
					// myZoomBar.setVisibility(VerticalSeekBar.VISIBLE);
					// mSensorManager.registerListener(GREX3DActivity.this,
					// mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
					// SensorManager.SENSOR_DELAY_FASTEST);
					// manager.requestLocationUpdates(providerName, 3000, 0,
					// listener);
				} else {
					// mSensorManager.unregisterListener(GREX3DActivity.this);
					// manager.removeUpdates(listener);
					resetUI();

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
				mGLView.requestRender();
			}
		});

		b2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				b1.setEnabled(true);
				b2.setEnabled(false);
				HelloOpenGLES20Renderer.mAngleX = 0.0f;
				HelloOpenGLES20Renderer.mAngleY = 0.0f;
				HelloOpenGLES20Renderer.pan = false;
				mGLView.requestRender();
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
		textview = new TextView(this);
		textview.setText("Start");
		LayoutParams paramstext = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		paramstext.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		paramstext.addRule(RelativeLayout.CENTER_HORIZONTAL);
		textview.setLayoutParams(paramstext);
		rel.addView(textview);
		rel.addView(myZoomBar, zoomBarParams);

		frame.addView(rel);

	}

	protected void setARprefs() {
		AR = true;
		manager.requestLocationUpdates(providerName, 0, 0, listener);
		HelloOpenGLES20Renderer.AR = true;
		b1.setVisibility(Button.INVISIBLE);
		b2.setVisibility(Button.INVISIBLE);
		s1.setChecked(true);
//		mCamera = Camera.open();
//		cameraCurrentlyLocked = defaultCameraId;
//		mPreview.setCamera(mCamera);
		mPreview.mSurfaceView.setVisibility(SurfaceView.VISIBLE);
		myZoomBar.setVisibility(VerticalSeekBar.VISIBLE);
		myZoomBar.setMax(100);
		myZoomBar.setProgress(100);
		myZoomBar.setEnabled(false);
		mSensorManager.registerListener(GREX3DActivity.this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
				SensorManager.SENSOR_DELAY_FASTEST);
		
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