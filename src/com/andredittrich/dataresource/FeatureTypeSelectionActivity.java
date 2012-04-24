package com.andredittrich.dataresource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.andredittrich.xml.XMLHandler;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FeatureTypeSelectionActivity extends ListActivity {
	private static String[] strings;
//	= { "A", "B", "C", "A", "B", "C",
//			"A", "B", "C", "A", "B", "C", "A", "B", "C", "A", "B", "C", "A",
//			"B", "C", "A", "B", "C", "A", "B", "C", "A", "B", "C", "A", "B",
//			"C", "..." };
	public static String data = "Mist";
	public TextView textview;
	public String[] serviceResponse;
	public FeatureTypeSelectionActivity parent = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			data = extras.getString(WFSSelectionActivity.CAP_URL);
		}
		
		Log.d("data", data);
//		setListAdapter(new ArrayAdapter<String>(this,
//				android.R.layout.simple_spinner_item, new String[] {}));
//		textview = (TextView) findViewById(R.id.WFSListTitle);
//		setContentView(R.layout.listviewwithbutton);
		readWebpage(getListView());
		
	}
	
	private class DownloadWebPageTask extends AsyncTask<String, Void, ArrayList<String>> {
		@Override
		protected ArrayList<String> doInBackground(String... urls) {
			ArrayList<String> response = null;
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();
		//
					XMLReader xr = sp.getXMLReader();
		//
					XMLHandler Handler = new XMLHandler("Name");
					xr.setContentHandler(Handler);
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();
					InputSource inSource = new InputSource(content);
					xr.parse(inSource);
					response = Handler.data;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
//			serviceResponse = result;
			serviceResponse = new String[result.size()];
			for (int i = 0; i < result.size(); i++) {
				serviceResponse[i] = result.get(i);				
			}
			
			Log.d("length", Integer.toString(serviceResponse.length));
			Log.d("result", serviceResponse[0]);
			
			parent.setListAdapter(new ArrayAdapter<String>(parent,
					android.R.layout.simple_spinner_item, serviceResponse));
			for (String tag : serviceResponse) {
				Log.d("achtung", tag);
			}
		}
	}

	public void readWebpage(View view) {
		DownloadWebPageTask task = new DownloadWebPageTask();
		task.execute(new String[] { data });
		
		

	}
}
