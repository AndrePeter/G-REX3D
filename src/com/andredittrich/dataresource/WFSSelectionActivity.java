package com.andredittrich.dataresource;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import com.andredittrich.database.SQLiteOnSD;

public class WFSSelectionActivity extends ListActivity implements
		OnItemClickListener {
	private SQLiteOnSD openHandler;
	SimpleCursorAdapter mAdapter;
	Button addWFS;
	public static String CAP_URL = "Capabilities URL";
	private static final int DIALOG_ADDWFS = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
		openHandler = new SQLiteOnSD(this);
		// openHandler.dropTable();
		// for (int i = 0; i<15;i++) {
		// openHandler.insert("Test" + i, "www.test.de", 1);
		// }
//		openHandler.insert("WFSDB4GeO",
//				 "http://192.168.0.100:8182/MyProject/wfs?");
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
		String url = w.getString(columnIndex)
				+ "REQUEST=GetCapabilities&VERSION=1.0.0";
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		Log.d("url", url);
		
		Intent intent = new Intent(WFSSelectionActivity.this, FeatureTypeSelectionActivity.class);
		intent.putExtra(CAP_URL, url);
		startActivity(intent);
		
//		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//		intent.setData(intent.getData());
//		Log.d("datastring",intent.getDataString());
//		startActivity(intent);
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
							mAdapter.getCursor().requery();
							mAdapter.notifyDataSetChanged();
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
			openHandler.delete(info.id);
			mAdapter.getCursor().requery();
			mAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	private void updateList() {
		// zunächst Cursor, dann Liste aktualisieren
		mAdapter.getCursor().requery();
		mAdapter.notifyDataSetChanged();
	}
}
