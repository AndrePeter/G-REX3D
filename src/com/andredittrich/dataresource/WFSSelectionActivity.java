package com.andredittrich.dataresource;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.andredittrich.database.SQLiteOnSD;
import com.andredittrich.xml.XMLHandler;

public class WFSSelectionActivity extends ListActivity implements
OnItemClickListener {
	
	private static SQLiteOnSD openHandler;
	private static SimpleCursorAdapter mAdapter;
	private Button addWFS;
	public static final String FEATURE_TYPES = "Feature Types";
	public static final String SEARCH_TAG = "Name";
	private static final int DIALOG_ADDWFS = 1;
	private static String chosenURL;
	public static String baseURL;
	public static String[] serviceResponse;
//	public final WFSSelectionActivity parent = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		openHandler = new SQLiteOnSD(this);
		// openHandler.dropTable();
		// for (int i = 0; i<15;i++) {
		// openHandler.insert("Test" + i, "www.test.de", 1);
		// }
		// openHandler.insert("WFSDB4GeO",
		// "http://192.168.0.100:8182/MyProject/wfs?");
		Cursor data = openHandler.query();

		// TODO: custom Adapter Class !!!
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, data, new String[] {
				SQLiteOnSD.WFS_NAME, SQLiteOnSD.WFS_URL }, new int[] {
				android.R.id.text1, android.R.id.text2 }, 0);
		setListAdapter(mAdapter);

		setContentView(R.layout.listviewwithbutton);
		getListView().setOnItemClickListener(this);
		addWFS = (Button) findViewById(R.id.AddWFSButton);
		addWFS.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_ADDWFS);
			}
		});
		registerForContextMenu(getListView());

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		openHandler.close();
		// stopManagingCursor(WebFeatureServices);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object o = getListAdapter().getItem(position);
		Cursor w = (Cursor) o;
		int columnIndex = w.getColumnIndex(SQLiteOnSD.WFS_URL);
		baseURL = w.getString(columnIndex);
		if (!baseURL.startsWith(getString(R.string.HTTP))) {
			baseURL = getString(R.string.HTTP) + baseURL;
		}
		chosenURL =	baseURL + getString(R.string.Capabilities) + "&" + getString(R.string.Version110);
		
		Log.d("url", chosenURL);
		readWebpage(getListView());
	}

	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ADDWFS:

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			View addWFSView = getLayoutInflater().inflate(
					R.layout.addwfsdialog, null);
			builder.setView(addWFSView);
			final EditText wfsName = (EditText) addWFSView
					.findViewById(R.id.WFSName);
			final EditText wfsBaseURL = (EditText) addWFSView
					.findViewById(R.id.WFSBaseURL);
			builder.setTitle(R.string.WFSDialogTitle);
			builder.setPositiveButton(R.string.AddWFSButtonText,
					new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					openHandler.insert(wfsName.getText().toString(),
							wfsBaseURL.getText().toString());
					updateList();
				}
			});

			builder.setNegativeButton(R.string.cancelItem,
					new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

			return builder.create();
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// Kontextmenü entfalten
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wfs_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.WFS_menu_delete:
			deleteRow(info.id);
			updateList();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void deleteRow(long id) {
		if (openHandler.delete(id) != 0) {
			Toast.makeText(this, "deleted", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
		}
	}

	private void updateList() {
		// zunächst Cursor, dann Liste aktualisieren
		mAdapter.getCursor().requery();
		mAdapter.notifyDataSetChanged();
	}

	private class DownloadWebPageTask extends
	AsyncTask<String, Void, ArrayList<String>> {
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
					XMLHandler Handler = new XMLHandler(SEARCH_TAG);
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
			serviceResponse = new String[result.size()];
			for (int i = 0; i < result.size(); i++) {
				serviceResponse[i] = result.get(i);
			}

			Log.d("length", Integer.toString(serviceResponse.length));
			Intent intent = new Intent(WFSSelectionActivity.this,
					FeatureTypeSelectionActivity.class);
			intent.putExtra(FEATURE_TYPES, serviceResponse);
			startActivity(intent);
		}
	}

	public void readWebpage(View view) {
		DownloadWebPageTask task = new DownloadWebPageTask();
		task.execute(new String[] { chosenURL });
	}

}
