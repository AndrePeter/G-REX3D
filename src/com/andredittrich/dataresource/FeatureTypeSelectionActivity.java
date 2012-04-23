package com.andredittrich.dataresource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

public class FeatureTypeSelectionActivity extends ListActivity {
	private static final String[] strings = { "A", "B", "C", "A", "B", "C",
			"A", "B", "C", "A", "B", "C", "A", "B", "C", "A", "B", "C", "A",
			"B", "C", "A", "B", "C", "A", "B", "C", "A", "B", "C", "A", "B",
			"C", "..." };
	public static String data = "Mist";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			data = extras.getString(WFSSelectionActivity.CAP_URL);
		}
		StringBuilder sb = new StringBuilder();
		HttpGet get = new HttpGet("http://www.gmx.net");
		HttpClient httpclient = new DefaultHttpClient();

		HttpResponse r;
		try {
			r = httpclient.execute(get);
			InputStream is = r.getEntity().getContent();
			Log.d("answer", is.toString());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// StringReader inStream = new StringReader(xmlString);
		// // InputSource inSource = new InputSource(inStream);
		// //
		// // xr.parse(inSource);

		Log.d("data", data);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, strings));
	}
}
