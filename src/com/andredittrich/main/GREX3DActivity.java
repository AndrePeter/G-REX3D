package com.andredittrich.main;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import com.andredittrich.xml.XMLHandler;

public class GREX3DActivity extends Activity {

	private HelloOpenGLES20SurfaceView mGLView;

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Document doc = null;
//
//		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		try {
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			Log.d("root", "1");
//			doc = db.parse(new File(Environment.getExternalStorageDirectory()
//					+ "/data/balingen.xml"));
//			String tagname = "gml:coordinates";
//			NodeList nodeList = doc.getElementsByTagName(tagname);
//
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				Node childNode = nodeList.item(i);
//				// Do something with childNode...
//				String name = childNode.getNodeName();
//				Log.d("root", name);
//			}
//			Log.d("root", "2");
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		 }
		//

		// sax stuff
//		try {
//			SAXParserFactory spf = SAXParserFactory.newInstance();
//			SAXParser sp = spf.newSAXParser();
//
//			XMLReader xr = sp.getXMLReader();
//
//			XMLHandler Handler = new XMLHandler();
//			xr.setContentHandler(Handler);
//
//			Log.d("root", "1");
////			String xmlString="<?xml version=\"1.0\" encoding=\"UTF-8\"?><note>Testnotiz</note>";
////			StringReader inStream = new StringReader(xmlString);
////			InputSource inSource = new InputSource(inStream);
////			
////			xr.parse(inSource);
//			xr.parse(new InputSource(new FileInputStream(Environment
//					.getExternalStorageDirectory() + "/data/balingen.xml")));
////			Log.d("wert", Integer.toString(Handler.data.size()));
//			Log.d("length", Integer.toString(Handler.data.size()));
////			Log.d("length", Handler.data.get(1));
//			int i = 0;
//			for (String tag : Handler.data) {
//				i++;
////				String[] leer = tag.split("\\s");
////				Log.d("achtung", Integer.toString(leer.length));
//				
//				Log.d("wertvoll", i + " " + tag );
////				Log.d("wert", leer[0] + " " + leer[1] + " " + leer[2]);
//			}
//			Log.d("root", "2");
//
//		} catch (ParserConfigurationException pce) {
//			Log.e("SAX XML", "sax parse error", pce);
//		} catch (SAXException se) {
//			Log.e("SAX XML", "sax error", se);
//		} catch (IOException ioe) {
//			Log.e("SAX XML", "sax parse io error", ioe);
//		}

		// Create a GLSurfaceView instance and set it
		// as the ContentView for this Activity
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