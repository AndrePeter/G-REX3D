package com.andredittrich.dataresource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.andredittrich.main.GREX3DActivity;

public class FeatureTypeSelectionActivity extends ListActivity {

	private static String[] intentData;
	private static SimpleAdapter adapter;
	private static String chosenTypeName;
	private static String describeURL;
	private static String getURL;

	private static final String ROW_ID_1 = "NAME";
	private static final String ROW_ID_2 = "TITLE";
	private static final String SEARCH_TAG_GETFEATURE = "gml:posList";
	private static final String SEARCH_TAG_DESCRIBE = "element";
	public static String serviceResponse;
	
	public static ProgressBar progressBar;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		intentData = getDataFromIntent();
		List<HashMap<String, String>> fillMaps = prepareData4List();

		adapter = new SimpleAdapter(this, fillMaps,
				android.R.layout.simple_list_item_2, new String[] { ROW_ID_1,
				ROW_ID_2 }, new int[] { android.R.id.text1,
				android.R.id.text2 });

		setListAdapter(adapter);

		setContentView(R.layout.listfeaturetypes);
		progressBar = (ProgressBar)findViewById(R.id.progressbar_Horizontal);
	    progressBar.setProgress(0);
		
//		setContentView(R.layout.listfeaturetypes);
		// getListView().setOnItemClickListener(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HashMap<String, String> o = (HashMap<String, String>) getListAdapter()
				.getItem(position);
		chosenTypeName = (String) o.get(ROW_ID_1);
		describeURL = WFSSelectionActivity.baseURL + getString(R.string.Describe) + "&"
				+ getString(R.string.Version110) + "&" + getString(R.string.Typename)
				+ chosenTypeName;

		getURL = WFSSelectionActivity.baseURL + getString(R.string.Feature) + "&"
				+ getString(R.string.Version110) + "&" + getString(R.string.Typename)
				+ chosenTypeName + "&OUTPUTFORMAT=GOCAD";
		Log.d("describeurl", describeURL);
		Log.d("geturl", getURL);
		readWebpage(getListView());
	}

	private List<HashMap<String, String>> prepareData4List() {
		List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
		for (String entry : intentData) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(ROW_ID_1, entry);
			map.put(ROW_ID_2, entry.split(":")[1]);
			fillMaps.add(map);
		}
		return fillMaps;
	}

	private String[] getDataFromIntent() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			intentData = extras
					.getStringArray(WFSSelectionActivity.FEATURE_TYPES);
			return intentData;
		} else {
			return null;
		}
	}

	private class DownloadWebPageTask extends
	AsyncTask<String, Void, ArrayList<String>> {
//		int myProgress;
		
		@Override
		protected ArrayList<String> doInBackground(String... urls) {
			ArrayList<String> response = new ArrayList<String>(0);
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
//					SAXParserFactory spf = SAXParserFactory.newInstance();
//					SAXParser sp = spf.newSAXParser();
//					//
//					XMLReader xr = sp.getXMLReader();
//					//
//					XMLHandler Handler = new XMLHandler(getSearchTag(url));
//					xr.setContentHandler(Handler);
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();
//					InputSource inSource = new InputSource(content);
//					xr.parse(inSource);
					
					String res = readInputStreamAsString(content);
					
					
					response.add(res);
					
					
//					Log.d("versuch", res);
					
					
					
//					while(myProgress<100){
//					    myProgress++;
//					    onProgressUpdate(myProgress);
//					       SystemClock.sleep(100);
//					   }
//					response = Handler.data;
						
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
						
			serviceResponse = result.toString();
			
			Intent intent = new Intent(FeatureTypeSelectionActivity.this, GREX3DActivity.class);
			intent.putExtra(getString(R.string.TSObject), serviceResponse);
			intent.putExtra("ResourceType", "WFS");
			startActivity(intent);

		}
	
//		protected void onProgressUpdate(Integer... values) {
//		   // TODO Auto-generated method stub
//		   progressBar.setProgress(values[0]);
//		  }
//		protected void onPreExecute() {
//			   // TODO Auto-generated method stub
//			   myProgress = 0;
//			  }
	}

	public void readWebpage(View view) {
		DownloadWebPageTask task = new DownloadWebPageTask();
		task.execute(new String[] { describeURL, getURL });
	}

	public String getSearchTag(String url) {
		// TODO Auto-generated method stub
		if (url.contains(getString(R.string.Describe))) {
			return SEARCH_TAG_DESCRIBE;
		} else {
			return SEARCH_TAG_GETFEATURE;
		}
	}
	
	public static String readInputStreamAsString(InputStream in) 
		    throws IOException {

		    BufferedInputStream bis = new BufferedInputStream(in);
		    ByteArrayOutputStream buf = new ByteArrayOutputStream();
		    int result = bis.read();
		    while(result != -1) {
		      byte b = (byte)result;
		      buf.write(b);
		      result = bis.read();
		    }
		    return buf.toString();
		}
}
