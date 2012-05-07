package com.andredittrich.dataresource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.andredittrich.main.GREX3DActivity;
import com.andredittrich.surface3d.GOCADConnector;
import com.andredittrich.surface3d.GOCADConnector.TSObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class DataOnSDSelection extends ListActivity {

	private static SimpleAdapter adapter;
	private static List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
	private static GOCADConnector connect3D = new GOCADConnector();
//	private static final String ROW_ID_1 = "NAME";
//	private static final String ROW_ID_2 = "GROESSE";
	private static String TSFileName;
	ProgressBar progressBar;
	File[] files = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		searchTSFiles();		
		prepareData4List();

		adapter = new SimpleAdapter(this, fillMaps,
				android.R.layout.simple_list_item_2, new String[] { getString(R.string.FileName),
				getString(R.string.FileSize) }, new int[] { android.R.id.text1,
				android.R.id.text2 });

		setListAdapter(adapter);

		setContentView(R.layout.listtsfiles);
		progressBar = (ProgressBar) findViewById(R.id.progressbar_Horizontal);
		progressBar.setProgress(0);
		registerForContextMenu(getListView());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// stopManagingCursor(WebFeatureServices);
	}

	private void searchTSFiles() {
		// File sdCardDirectory = Environment.getExternalStorageDirectory();
		File dir = new File(ResourceSelectionActivity.rootDirectory);
		FilenameFilter filter = new FileListFilter(null, "ts");
		files = dir.listFiles(filter);
		if (files.length == 0) {
			Toast.makeText(this, "no suitable data", Toast.LENGTH_LONG).show();
			finish();
		}
//		for (File file : files) {
//			Log.d("datei", file.getName());
//			Log.d("laenge", Long.toString(file.length()));
//		}

	}

	@SuppressWarnings("unchecked")
	public void onListItemClick(ListView l, View view, int position, long id) {
		HashMap<String, String> o = (HashMap<String, String>) getListAdapter()
				.getItem(position);
		TSFileName = (String) o.get(getString(R.string.FileName));		
		File TSFile = new File(ResourceSelectionActivity.rootDirectory + "/" + TSFileName);
		
		Intent intent = new Intent(DataOnSDSelection.this, GREX3DActivity.class);				
		intent.putExtra(getString(R.string.TSObject), TSFile.getPath());
		startActivity(intent);
		
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// Kontextmenü entfalten
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.ts_context_menu, menu);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.ts_menu_delete:
			// TODO file deletion
			HashMap<String, String> o = (HashMap<String, String>) getListAdapter()
			.getItem(info.position);
			String selectedTSFile = (String) o.get(getString(R.string.FileName));
			deleteTSFile(selectedTSFile);
			updateList();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void deleteTSFile(String selectedTSFile) {
		boolean success = (new File(ResourceSelectionActivity.rootDirectory
				+ "/" + selectedTSFile)).delete();
		if (!success) {
			Toast.makeText(this, "failed", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "deleted", Toast.LENGTH_LONG).show();
		}
	}

	private void updateList() {
		searchTSFiles();
		prepareData4List();
		adapter.notifyDataSetChanged();
	}

	private void prepareData4List() {
		fillMaps.clear();
		for (File file : files) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(getString(R.string.FileName), file.getName());
			map.put(getString(R.string.FileSize), Long.toString(file.length()) + " Bytes");
			fillMaps.add(map);
		}
	}

	class FileListFilter implements FilenameFilter {
		private String name;

		private String extension;

		public FileListFilter(String name, String extension) {
			this.name = name;
			this.extension = extension;
		}

		public boolean accept(File directory, String filename) {
			boolean fileOK = true;

			if (name != null) {
				fileOK &= filename.startsWith(name);
			}

			if (extension != null) {
				fileOK &= filename.endsWith('.' + extension);
			}
			return fileOK;
		}
	}

}
