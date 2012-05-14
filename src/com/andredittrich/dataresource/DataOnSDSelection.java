package com.andredittrich.dataresource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.andredittrich.main.GREX3DActivity;

public class DataOnSDSelection extends ListActivity {

	/**
	 * Adapter to fill ListView with existing .ts-files
	 */
	private static SimpleAdapter adapter;

	/**
	 * List of HashMaps to hold name and size key-value pairs for ListView
	 */
	private static List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

	/**
	 * String to hold the path to the data folder
	 */
	private static String dataPath;

	/**
	 * String to hold the name of the .ts-file selected by the user
	 */
	private static String TSFileName;

	/**
	 * String array to hold the names of all found .ts-files
	 */
	private static File[] files = null;

	// private static GOCADConnector connect3D = new GOCADConnector();
	// ProgressBar progressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataPath = ResourceSelectionActivity.ROOT_DIRECTORY + File.separator
		 +
		 getString(R.string.DataFolder);
		searchTSFiles();

		prepareData4List();

		adapter = initAdapter();
		setListAdapter(adapter);

		setContentView(R.layout.listtsfiles);
		// progressBar = (ProgressBar)
		// findViewById(R.id.progressbar_Horizontal);
		// progressBar.setProgress(0);
		registerForContextMenu(getListView());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void searchTSFiles() {
		File dir = new File(dataPath);
		Log.d("data", dir.toString());
		FilenameFilter filter = new FileListFilter(null, new String[] {"ts","xml"});
		files = dir.listFiles(filter);
		if (files.length == 0) {
			Toast.makeText(this, R.string.NoData, Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private SimpleAdapter initAdapter() {

		return new SimpleAdapter(this, fillMaps,
				android.R.layout.simple_list_item_2, new String[] {
				getString(R.string.FileName),
				getString(R.string.FileSize) }, new int[] {
				android.R.id.text1, android.R.id.text2 });
	}

	private void prepareData4List() {
		fillMaps.clear();
		for (File file : files) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(getString(R.string.FileName), file.getName());
			map.put(getString(R.string.FileSize), Long.toString(file.length())
					+ " Bytes");
			fillMaps.add(map);
		}
	}

	private void deleteTSFile(String selectedTSFile) {
		boolean success = (new File(dataPath + File.separator + selectedTSFile))
				.delete();
		if (!success) {
			Toast.makeText(this, R.string.FAILED, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, R.string.DELETED, Toast.LENGTH_LONG).show();
		}
	}

	private void updateList() {
		searchTSFiles();
		prepareData4List();
		adapter.notifyDataSetChanged();
	}

	@SuppressWarnings("unchecked")
	public void onListItemClick(ListView l, View view, int position, long id) {
		HashMap<String, String> o = (HashMap<String, String>) getListAdapter()
				.getItem(position);
		TSFileName = (String) o.get(getString(R.string.FileName));
		Intent intent = new Intent(DataOnSDSelection.this, GREX3DActivity.class);
		intent.putExtra(getString(R.string.TSObject), dataPath + File.separator
				+ TSFileName);
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
			String selectedTSFile = (String) o
					.get(getString(R.string.FileName));
			deleteTSFile(selectedTSFile);
			updateList();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private class FileListFilter implements FilenameFilter {
		private String name;

		private List<String> extension = new ArrayList<String>();

		public FileListFilter(String name, String[] ext) {
			this.name = name;
			this.extension = Arrays.asList(ext);
		}

		public boolean accept(File directory, String filename) {
			boolean fileOK = true;

			if (name != null) {
				fileOK &= filename.startsWith(name);
			}

			if (extension != null) {

				if (filename.contains(".")) {
					String[] splitname = filename.split("\\.");
					fileOK &= extension.contains(splitname[1]);
				} else {
					fileOK = false;
				}
			}
			return fileOK;
		}
	}
}
